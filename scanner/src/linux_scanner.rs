use std::io::{BufWriter, Read, Write};
use std::fs::{read_dir, read_link, DirEntry, File, metadata};
use std::os::linux::fs::MetadataExt;
use std::os::unix::fs::PermissionsExt;
use std::path::{Path, PathBuf};
use byteorder::{BigEndian, WriteBytesExt};
use crc32fast::Hasher;
use std::env;
use std::sync::mpsc::{sync_channel, SyncSender, Receiver};
use std::thread::{spawn, sleep};
use std::time::{Duration, SystemTime, Instant};
use crate::scanner::{Progress, write_entry};

pub fn read_file(id: &u64, parent_id: &u64, entry: &DirEntry, buf: &mut BufWriter<File>, log: &SyncSender<Progress>) {
    let name = entry.file_name();
    let name = name.to_str().unwrap();
    let path = entry.path().as_os_str().to_str().unwrap().to_owned() + name;

    let _ = log.try_send(Progress { id: *id, name: path.to_string() });

    let name = name.as_bytes();

    let mut flags: u8 = 0b00000000;
    let mut mode: Option<u32> = None;
    let mut uid: Option<u32> = None;
    let mut gid: Option<u32> = None;
    let mut size: Option<u64> = None;
    let mut created: Option<u64> = None;
    let mut modified: Option<u64> = None;
    let mut hash: Option<u32> = None;
    let mut link_dest: Option<PathBuf> = None;

    let unix_meta = metadata(entry.path());
    if unix_meta.is_ok() {
        let meta = unix_meta.unwrap();
        let file_type = meta.file_type();
        let perms = meta.permissions();
        uid = Some(meta.st_uid());
        gid = Some(meta.st_gid());

        if file_type.is_dir() {
            flags |= 0b00000001;
        }
        if file_type.is_file() {
            flags |= 0b00000010;
        }
        if file_type.is_symlink() {
            flags |= 0b00000100;
        }
        if perms.readonly() {
            flags |= 0b00001000;
        }
        mode = Some(perms.mode());

        created = match meta.created() {
            Err(_) => None,
            Ok(x) => Some(x.elapsed().unwrap().as_secs()),
        };

        modified = match meta.modified() {
            Err(_) => None,
            Ok(x) => Some(x.elapsed().unwrap().as_secs()),
        };

        link_dest = read_link(entry.path()).ok();
        if file_type.is_file() {
            let mut hasher = Hasher::new();
            let mut scanned_file = File::open(entry.path()).expect("Error opening file for hashing!");
            let mut buffer = [0u8; 1024];

            let mut count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
            let mut start_time = Instant::now();
            while count > 0 {
                count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
                hasher.update(&buffer[0..count]);
                if start_time.elapsed().as_secs() > 5 {
                    let _ = log.try_send(Progress { id: *id, name: path.to_string(), cont: true });
                    start_time = Instant::now();
                }
            }
            hash = Some(hasher.finalize());
        }
    }
    write_entry(buf, id, parent_id, name, flags, size, created, modified, link_dest, mode, &hash);
}


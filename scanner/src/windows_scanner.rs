use std::fs::{DirEntry, File, metadata, read_link};
use std::io::{BufWriter, Read};
use std::sync::mpsc::SyncSender;
use crc32fast::Hasher;
use crate::scanner::{Progress,write_entry};
use std::path::PathBuf;

pub fn read_file(id: &u64, parent_id: &u64, entry: &DirEntry, buf: &mut BufWriter<File>, log: &SyncSender<Progress>) {
    let name = entry.file_name();
    let name = name.to_str().unwrap();

    log.try_send(Progress { id: *id, name: name.to_string() });

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
        let meta: std::os::linux::fs::MetadataExt = unix_meta.unwrap();
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
            while count > 0 {
                count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
                hasher.update(&buffer[0..count]);
            }
            hash = Some(hasher.finalize());
        }
    }
    scanner::write_entry(buf, id, parent_id, name, flags, size, created, modified, link_dest, mode, &hash);
}


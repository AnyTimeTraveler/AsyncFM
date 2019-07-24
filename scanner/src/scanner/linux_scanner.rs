use std::fs;
use std::io::BufWriter;
use std::os::linux::fs::MetadataExt;
use std::sync::mpsc::SyncSender;

use crate::scanner::{FileMetadata, Progress, write_entry};

pub fn read_file(id: &u64, parent_id: &u64, entry: &fs::DirEntry, buf: &mut BufWriter<fs::File>, log: &SyncSender<Progress>) {
    let name = entry.file_name();
    let name = name.to_str().unwrap();
    let path = entry.path().as_os_str().to_str().unwrap().to_owned();

    let _ = log.try_send(Progress { id: *id, name: path.to_string() });

    let mut link_dest;

    let file = match entry.path().metadata() {
        Err(_) => FileMetadata {
            id: *id,
            parent_id: *parent_id,
            name: name.as_bytes(),
            flags: 0b10000000,
            mode: 0,
            uid: 0,
            gid: 0,
            size: 0,
            created: 0,
            modified: 0,
            accessed: 0,
            link_dest: &[0u8;0],
            hash: 0,
        },
        Ok(meta) => {
            let file_type = meta.file_type();
            let mut flags = 0b00000000;

            if file_type.is_dir() {
                flags |= 0b00000001;
            }
            if file_type.is_file() {
                flags |= 0b00000010;
            }
            if file_type.is_symlink() {
                flags |= 0b00000100;
            }

            FileMetadata {
                id: *id,
                parent_id: *parent_id,
                name: name.as_bytes(),
                flags,
                mode: meta.st_mode(),
                uid: meta.st_uid(),
                gid: meta.st_gid(),
                size: meta.st_size(),
                created: meta.st_ctime(),
                modified: meta.st_mtime(),
                accessed: meta.st_atime(),
                link_dest: match fs::read_link(entry.path()) {
                    Ok(dest) => {
                        link_dest = dest.to_str().unwrap().as_bytes().to_owned();
                        &link_dest
                    },
                    Err(_) => &[0u8;0],
                },
                hash: 0,
            }
        }
    };

    write_entry(buf, &file);
}


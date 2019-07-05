use std::io::{BufWriter, Read, Write};
use std::fs::{read_dir, read_link, DirEntry, File, metadata};
use std::path::{Path, PathBuf};
use byteorder::{BigEndian, WriteBytesExt};
use crc32fast::Hasher;
use std::env;
use std::sync::mpsc::{sync_channel, SyncSender, Receiver};
use std::thread::{spawn, sleep};
use std::time::Duration;

pub struct Progress {
    pub id: u64,
    pub name: String,
}

pub fn visit_dirs(parent_id: u64,
                  dir: &Path,
                  mut buf: &mut BufWriter<File>,
                  log: &SyncSender<Progress>,
                  read_file: fn(id: &u64, parent_id: &u64, entry: &DirEntry, buf: &mut BufWriter<File>, log: &SyncSender<Progress>)) -> u64 {
    let mut id = parent_id + 1;
    let dir_info = read_dir(dir);
    if dir_info.is_err() {
        return id;
    }
    let dir_info = dir_info.unwrap();
    for entry in dir_info {
        if entry.is_err() {
            eprintln!("Couldn't read entry: {}", entry.unwrap_err());
            continue;
        }
        let entry = entry.unwrap();
        let file_type = entry.file_type();
        if file_type.is_err() {
            eprintln!("Couldn't determine filetype of: {:?} {}", entry.path(), file_type.unwrap_err());
            continue;
        }
        let file_type = file_type.unwrap();
        let path = entry.path();

        read_file(&id, &parent_id, &entry, &mut buf, log);
        if path.is_dir() && !file_type.is_symlink() {
            id = visit_dirs(id, &path, buf, log, read_file);
        }
        id += 1;
    }
    return id;
}

pub fn write_entry(buf: &mut BufWriter<File>,
               id: &u64,
               parent_id: &u64,
               name: &[u8],
               flags: u8,
               size: Option<u64>,
               created: Option<u64>,
               modified: Option<u64>,
               link_dest: Option<PathBuf>,
               mode: Option<u32>,
               hash: &Option<u32>) {
    buf.write_u64::<BigEndian>(*id).expect("Error writing id!");
    buf.write_u64::<BigEndian>(*parent_id).expect("Error writing parent id!");
    buf.write_all(name).expect("Error writing name!");
    buf.write_u8(0u8);

    buf.write_u8(flags).expect("Error writing flags!");
    match mode {
        Some(data) => buf.write_u32::<BigEndian>(data).expect("Error writing permissions!"),
        None => buf.write_u32::<BigEndian>(0u32).expect("Error writing permissions (0)!")
    }
    match modified {
        Some(data) => buf.write_u64::<BigEndian>(data).expect("Error writing modified!"),
        None => buf.write_u64::<BigEndian>(0u64).expect("Error writing modified (0)!")
    }
    match created {
        Some(data) => buf.write_u64::<BigEndian>(data).expect("Error writing created!"),
        None => buf.write_u64::<BigEndian>(0u64).expect("Error writing created (0)!")
    }
    match hash {
        Some(data) => buf.write_u32::<BigEndian>(*data).expect("Error while writing hash!"),
        None => buf.write_u32::<BigEndian>(0u32).expect("Error while writing hash (0)!")
    }

    match link_dest {
        Some(data) => {
            let data = data.to_str().unwrap().as_bytes();
            buf.write_all(data).expect("Error writing name!");
            buf.write_u8(0u8);
        }
        None => ()
    }
}



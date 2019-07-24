use std::fs::{File, read_dir};
use std::io::{BufWriter, Write};
use std::path::Path;
use std::sync::mpsc::SyncSender;

use byteorder::{BigEndian, WriteBytesExt};

#[cfg(unix)]
use linux_scanner::read_file;
#[cfg(windows)]
use windows_scanner::read_file;

#[cfg(unix)]
mod linux_scanner;
#[cfg(windows)]
mod windows_scanner;

pub struct Progress {
    pub id: u64,
    pub name: String,
}

pub struct FileMetadata<'t> {
    id: u64,
    parent_id: u64,
    name: &'t[u8],
    flags: u8,
    mode: u32,
    uid: u32,
    gid: u32,
    size: u64,
    created: i64,
    modified: i64,
    accessed: i64,
    link_dest: Option<Vec<u8>>,
    hash: Option<u32>,
}

pub fn visit_dirs(parent_id: u64,
                  dir: &Path,
                  mut buf: &mut BufWriter<File>,
                  log: &SyncSender<Progress>) -> u64{
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
            id = visit_dirs(id, &path, buf, log);
        }
        id += 1;
    }
    return id;
}

pub fn write_entry(buf: &mut BufWriter<File>, file: &FileMetadata) {
    buf.write_u64::<BigEndian>(file.id).expect("Error writing id!");
    buf.write_u64::<BigEndian>(file.parent_id).expect("Error writing parent id!");
    buf.write_all(file.name).expect("Error writing name!");
    buf.write_u8(0u8).expect("Error writing null byte at the end of a name string!");

    buf.write_u8(file.flags).expect("Error writing flags!");
    buf.write_u32::<BigEndian>(file.mode).expect("Error writing mode!");
    buf.write_u32::<BigEndian>(file.uid).expect("Error writing uid!");
    buf.write_u32::<BigEndian>(file.gid).expect("Error writing gid!");
    buf.write_u64::<BigEndian>(file.size).expect("Error writing size!");
    buf.write_i64::<BigEndian>(file.created).expect("Error writing created!");
    buf.write_i64::<BigEndian>(file.modified).expect("Error writing modified!");
    buf.write_i64::<BigEndian>(file.accessed).expect("Error writing accessed!");

    match &file.link_dest {
        Some(data) => buf.write_all(&data).expect("Error writing name!"),
        None => ()
    }
    buf.write_u8(0u8).expect("Error writing null byte at the end of a link dest string!");
    match file.hash {
        Some(data) => buf.write_u32::<BigEndian>(data).expect("Error while writing hash!"),
        None => buf.write_u32::<BigEndian>(0u32).expect("Error while writing hash (0)!")
    }
}



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

#[derive(Debug)]
pub struct FileMetadata<'t> {
    id: u64,
    parent_id: u64,
    name: &'t [u8],
    /**
    Flags: [1234 5678]

    1: reserved
    2: reserved
    3: reserved
    4: reserved
    5: true if hash exists
    6: true if symlink
    7: true if file
    8: true if directory
    */
    flags: u8,
    /**
    Mode: standard linux mode data.
    */
    mode: u32,
    uid: u32,
    gid: u32,
    size: u64,
    created: i64,
    modified: i64,
    accessed: i64,
    link_dest: &'t [u8],
    hash: u32,
}

pub fn scan_directory(dir: &Path,
                      mut buf: &mut BufWriter<File>,
                      log: &SyncSender<Progress>) -> u64 {
    read_file(&0, &0, &dir.to_path_buf(), &mut buf, log);
    visit_dirs(0,dir,buf,log)
}

fn visit_dirs(parent_id: u64,
              dir: &Path,
              mut buf: &mut BufWriter<File>,
              log: &SyncSender<Progress>) -> u64 {
    let mut id = parent_id + 1;
    let dir_info = read_dir(dir);
    if dir_info.is_err() {
        eprintln!("Error reading file: {}", dir.to_str().unwrap());
        return parent_id;
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
            eprintln!("Couldn't determine file type of: {:?} {}", entry.path(), file_type.unwrap_err());
            continue;
        }
        let file_type = file_type.unwrap();
        let path = entry.path();

        read_file(&id, &parent_id, &path, &mut buf, log);
        if path.is_dir() && !file_type.is_symlink() {
            id = visit_dirs(id, &path, buf, log);
        }
        if !path.is_dir() {
            id += 1;
        }
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

    buf.write_all(file.link_dest).expect("Error writing name!");
//    println!("{}", file.link_dest);
    buf.write_u8(0u8).expect("Error writing null byte at the end of a link dest string!");
    buf.write_u32::<BigEndian>(file.hash).expect("Error while writing hash!");
}

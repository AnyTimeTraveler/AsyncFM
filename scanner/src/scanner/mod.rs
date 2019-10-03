use std::fs::{File, OpenOptions, read_dir};
use std::io::{BufWriter, Seek, SeekFrom, Write};
use std::path::Path;
use std::sync::mpsc::SyncSender;

use byteorder::{BigEndian, WriteBytesExt};

#[cfg(unix)]
use linux_scanner::read_file;
#[cfg(windows)]
use windows_scanner::read_file;
use crate::Options;

#[cfg(unix)]
mod linux_scanner;
#[cfg(windows)]
mod windows_scanner;

pub(crate) struct Progress {
    pub id: u64,
    pub name: String,
}

#[derive(Debug)]
pub(crate) struct Header<'t> {
    pub(crate) version: u32,
    pub(crate) flags: u8,
    /**
    Flags: [1234 5678]

    1: reserved
    2: reserved
    3: reserved
    4: reserved
    5: reserved
    6: reserved
    7: reserved
    8: true if image has hashes
    */
    pub(crate) entries: u64,
    pub(crate) base_path: &'t [u8],
}

#[derive(Debug)]
pub(crate) struct FileMetadata<'t> {
    pub(crate) id: u64,
    pub(crate) parent_id: u64,
    pub(crate) name: &'t [u8],
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
    pub(crate) flags: u8,
    /**
    Mode: standard linux mode data.
    */
    pub(crate) mode: u32,
    pub(crate) uid: u32,
    pub(crate) gid: u32,
    pub(crate) size: u64,
    pub(crate) created: i64,
    pub(crate) modified: i64,
    pub(crate) accessed: i64,
    pub(crate) link_dest: &'t [u8],
    pub(crate) hash: u32,
}

pub(crate) fn scan_directory(options: &Options, log: &SyncSender<Progress>) -> u64 {
    let directory_to_scan = Path::new(&options.source_folder);
    let output_file = OpenOptions::new()
        .read(false)
        .write(true)
        .create(true)
        .open(Path::new(&options.target_file))
        .expect("Can't open target file!");

    let mut buf = BufWriter::new(output_file);

    let mut header = Header {
        version: 1u32,
        flags: 0u8,
        entries: 0u64,
        base_path: directory_to_scan.to_str().unwrap().as_bytes(),
    };

    write_header(&header, &mut buf);

    read_file(&0, &0, &directory_to_scan.to_path_buf(), &mut buf, log);
    let entries_read_count = visit_dirs(0, directory_to_scan, &mut buf, log);
    buf.flush().expect("Error flushing target file!");

    header.entries = entries_read_count;

    update_header(&header, &mut buf);
    buf.flush().expect("Error flushing target file!");

    entries_read_count
}

pub(crate) fn write_header(header: &Header, buf: &mut BufWriter<File>) {
    buf.seek(SeekFrom::Start(0)).expect("Error seeking in file!");
    buf.write_u32::<BigEndian>(header.version).expect("Error writing header!");
    buf.write_u8(header.flags).expect("Error writing flags!");
    buf.write_u64::<BigEndian>(header.entries).expect("Error writing entries!");
    buf.write_all(header.base_path).expect("Error writing base path!");
    buf.write_u8(0u8).expect("Error writing string end byte!");
}


fn update_header(header: &Header, buf: &mut BufWriter<File>) {
    buf.seek(SeekFrom::Start(5)).expect("Error seeking in file!");
    buf.write_u64::<BigEndian>(header.entries).expect("Error writing entries!");
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

fn write_entry(buf: &mut BufWriter<File>, file: &FileMetadata) {
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

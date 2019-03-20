extern crate crc32fast;

use std::io::{self, BufWriter, Write, BufReader};
use std::fs::{self, DirEntry, File};
use std::io::prelude::*;
use std::path::Path;
use byteorder::{BigEndian, WriteBytesExt};
use crc32fast::Hasher;

// one possible implementation of walking a directory only visiting files
fn visit_dirs(dir: &Path, mut buf: &mut BufWriter<File>) -> io::Result<()> {
//    for each entry in directory
    for entry in fs::read_dir(dir)? {
//        return error if entry has issues
        let entry = entry?;
        let path = entry.path();
        if path.is_dir() {
            visit_dirs(&path, buf)?;
        } else {
            write(&entry, &mut buf);
        }
    }
    Ok(())
}

fn write(dir: &DirEntry, buf: &mut BufWriter<File>) {
    let temp = dir.file_name();
    let name = temp.to_str()?;
    println!("Reading {}", name);

    let name = name.as_bytes();
    let mut flags: u8 = 0;
    let meta = dir.metadata()?;
    let file_type = meta.file_type();
    let perms = meta.permissions();

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

    buf.write_u64::<BigEndian>(name.len() as u64)?;
    buf.write_all(name)?;
    buf.write_u64::<BigEndian>(meta.len())?;
    buf.write_u8(flags)?;
    buf.write_u64::<BigEndian>(meta.accessed()?.elapsed()?.as_secs())?;
    let created = match meta.created() {
        Err(_) => 0,
        Ok(x) => x.elapsed()?.as_secs(),
    };
    buf.write_u64::<BigEndian>(created)?;

    let mut hasher = Hasher::new();
    if file_type.is_file() {
        let mut scanned_file = File::open(dir.path())?;
        let mut buffer = [0; 1024];

        let mut count = scanned_file.read(&mut buffer[..])?;
        while count > 0 {
            count = scanned_file.read(&mut buffer[..])?;
            hasher.update(&buffer[0..count]);
        }
        buf.write_u32::<BigEndian>(hasher.finalize());
        return;
    }

    buf.write_u32::<BigEndian>(0);
}

fn main() {
    let mut output_file: BufWriter<File> = BufWriter::new(File::create("target.data").expect("Problems with output file."));
    visit_dirs(Path::new("/mnt/data/"), &mut output_file)?;

    output_file.flush()?;
}

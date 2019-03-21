extern crate crc32fast;

use std::io::{self, BufWriter, Write};
use std::fs::{self, DirEntry, File};
use std::io::prelude::*;
use std::path::Path;
use std::fs::read_link;
use byteorder::{BigEndian, WriteBytesExt};
use crc32fast::Hasher;
use std::env;


fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() < 3 {
        println!("Usage: <target file> [sources...]");
        return;
    }
    println!("OUT: {}", &args[1]);

    let output_file = File::create(&args[1]).expect("Can't open target file.");

    let mut output_file: BufWriter<File> = BufWriter::new(output_file);

    for i in 2..args.len() {
        match visit_dirs(Path::new(&args[i]), &mut output_file) {
            Ok(_) => println!("Done scanning {}!", &args[i]),
            Err(e) => {
                println!("Done scanning {}!", &args[i]);
                eprintln!("{}", e)
            }
        };
    }

    output_file.flush().expect("Error flushing target file!");
}


// one possible implementation of walking a directory only visiting files
fn visit_dirs(dir: &Path, mut buf: &mut BufWriter<File>) -> io::Result<()> {
//    for each entry in directory
    for entry in fs::read_dir(dir)? {
//        return error if entry has issues
        let entry = entry?;
        let path = entry.path();

        if entry.metadata()?.file_type().is_symlink() {
            write_symlink(&entry, &mut buf);
        } else if path.is_dir() {
            visit_dirs(&path, buf)?;
        } else {
            write(&entry, &mut buf);
        }
    }
    Ok(())
}

fn write_symlink(entry: &DirEntry, buf: &mut BufWriter<File>) -> io::Result<()> {
    let name = entry.file_name();
    let name = name.to_str().unwrap();
    println!("Reading symlink: {}", name);

    let name = name.as_bytes();
    let mut flags: u8 = 0b00000100;
    let meta = entry.metadata().unwrap();
    let file_type = meta.file_type();
    let perms = meta.permissions();

    if file_type.is_dir() {
        flags |= 0b00000001;
    }
    if file_type.is_file() {
        flags |= 0b00000010;
    }
    if perms.readonly() {
        flags |= 0b00001000;
    }

    buf.write_u64::<BigEndian>(name.len() as u64).expect("Error writing name length!");
    buf.write_all(name).expect("Error writing name!");
    buf.write_u64::<BigEndian>(meta.len()).expect("Error writing file size!");
    buf.write_u8(flags).expect("Error writing flags!");
    buf.write_u64::<BigEndian>(meta.accessed().unwrap().elapsed().unwrap().as_secs()).expect("Error writing access time!");

    let created = match meta.created() {
        Err(_) => 0,
        Ok(x) => x.elapsed().unwrap().as_secs(),
    };
    buf.write_u64::<BigEndian>(created).expect("Error writing creation time!");

    let dest = read_link(entry.path())?;
    let dest = dest.as_path();
    let dest = dest.to_str().unwrap();
    let dest = dest.as_bytes();

    buf.write_u64::<BigEndian>(dest.len() as u64).expect("Error writing name length!");
    buf.write_all(dest).expect("Error writing name!");

    buf.write_u32::<BigEndian>(0).expect("Error while writing hash (0)!");
    Ok(())
}

fn write(entry: &DirEntry, buf: &mut BufWriter<File>) -> io::Result<()> {
    let name = entry.file_name();
    let name = name.to_str().unwrap();
    println!("Reading:  {}", name);

    let name = name.as_bytes();
    let mut flags: u8 = 0;
    let meta = entry.metadata().unwrap();
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

    buf.write_u64::<BigEndian>(name.len() as u64).expect("Error writing name length!");
    buf.write_all(name).expect("Error writing name!");
    buf.write_u64::<BigEndian>(meta.len()).expect("Error writing file size!");
    buf.write_u8(flags).expect("Error writing flags!");
    buf.write_u64::<BigEndian>(meta.accessed().unwrap().elapsed().unwrap().as_secs()).expect("Error writing access time!");

    let created = match meta.created() {
        Err(_) => 0,
        Ok(x) => x.elapsed().unwrap().as_secs(),
    };
    buf.write_u64::<BigEndian>(created).expect("Error writing creation time!");

    let mut hasher = Hasher::new();
    if file_type.is_file() {
        let mut scanned_file = File::open(entry.path()).expect("Error opening file for hashing!");
        let mut buffer = [0; 1024];

        let mut count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
        while count > 0 {
            count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
            hasher.update(&buffer[0..count]);
        }
        buf.write_u32::<BigEndian>(hasher.finalize()).expect("Error while writing hash!");
        return Ok(());
    }

    buf.write_u32::<BigEndian>(0).expect("Error while writing hash (0)!");
    Ok(())
}

extern crate crc32fast;

use std::{env, thread};
use std::fs::{self, DirEntry, File};
use std::fs::read_link;
use std::io::{self, BufWriter, Write};
use std::io::prelude::*;
use std::path::Path;

use byteorder::{BigEndian, WriteBytesExt};
use crc32fast::Hasher;
use std::sync::Mutex;
use core::time;

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() < 3 {
        println!("Usage: <target file> [sources...]");
        return;
    }
    println!("Writing to: {}", &args[1]);

    let mut scanner = FSScanner::new(&args[1]);

    for i in 2..args.len() {
        match scanner.visit_dirs(Path::new(&args[i])) {
            Ok(_) => println!("Done scanning {}!", &args[i]),
            Err(e) => {
                println!("Done scanning {}!", &args[i]);
                eprintln!("{}", e)
            }
        };
    }

    scanner.finalize();
}


struct FSScanner {
    writer: BufWriter<File>,
    path: Mutex<String>,
    running: bool,
}

impl FSScanner {
    pub fn new(output_path: &str) -> FSScanner {
        let file = File::create(output_path).expect("Can't open target file.");
        let scanner = FSScanner {
            path: Mutex::new(String::from("")),
            writer: BufWriter::new(file),
            running: true,
        };
        thread::spawn(|| {
            let sleep_time = time::Duration::from_millis(250);
            while scanner.running {
                println!("Reading: {}", scanner.path.lock().unwrap());
                thread::sleep(sleep_time);
            }
        });

        return scanner;
    }

    pub fn finalize(&mut self){
        self.writer.flush().expect("Error flushing target file!");
        self.running = false;
    }


    // one possible implementation of walking a directory only visiting files
    pub fn visit_dirs(&mut self, dir: &Path) -> io::Result<()> {
//    for each entry in directory
        for entry in fs::read_dir(dir)? {
//        return error if entry has issues
            let entry = entry?;
            let path = entry.path();

            if entry.metadata()?.file_type().is_symlink() {
                self.write_symlink(&entry)?;
            } else if path.is_dir() {
                self.visit_dirs(&path)?;
            } else {
                self.write(&entry)?;
            }
        }
        Ok(())
    }

    fn write_symlink(&mut self, entry: &DirEntry) -> io::Result<()> {
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

        self.writer.write_u64::<BigEndian>(name.len() as u64).expect("Error writing name length!");
        self.writer.write_all(name).expect("Error writing name!");
        self.writer.write_u64::<BigEndian>(meta.len()).expect("Error writing file size!");
        self.writer.write_u8(flags).expect("Error writing flags!");
        self.writer.write_u64::<BigEndian>(meta.accessed().unwrap().elapsed().unwrap().as_secs()).expect("Error writing access time!");

        let created = match meta.created() {
            Err(_) => 0,
            Ok(x) => x.elapsed().unwrap().as_secs(),
        };
        self.writer.write_u64::<BigEndian>(created).expect("Error writing creation time!");

        let dest = read_link(entry.path())?;
        let dest = dest.as_path();
        let dest = dest.to_str().unwrap();
        let dest = dest.as_bytes();

        self.writer.write_u64::<BigEndian>(dest.len() as u64).expect("Error writing name length!");
        self.writer.write_all(dest).expect("Error writing name!");

        self.writer.write_u32::<BigEndian>(0).expect("Error while writing hash (0)!");
        Ok(())
    }

    fn write(&mut self, entry: &DirEntry) -> io::Result<()> {
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

        self.writer.write_u64::<BigEndian>(name.len() as u64).expect("Error writing name length!");
        self.writer.write_all(name).expect("Error writing name!");
        self.writer.write_u64::<BigEndian>(meta.len()).expect("Error writing file size!");
        self.writer.write_u8(flags).expect("Error writing flags!");
        self.writer.write_u64::<BigEndian>(meta.accessed().unwrap().elapsed().unwrap().as_secs()).expect("Error writing access time!");

        let created = match meta.created() {
            Err(_) => 0,
            Ok(x) => x.elapsed().unwrap().as_secs(),
        };
        self.writer.write_u64::<BigEndian>(created).expect("Error writing creation time!");

        let mut hasher = Hasher::new();
        if file_type.is_file() {
            let mut scanned_file = File::open(entry.path()).expect("Error opening file for hashing!");
            let mut buffer = [0; 1024];

            let mut count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
            while count > 0 {
                count = scanned_file.read(&mut buffer[..]).expect("Error while reading file for hashing!");
                hasher.update(&buffer[0..count]);
            }
            self.writer.write_u32::<BigEndian>(hasher.finalize()).expect("Error while writing hash!");
            return Ok(());
        }

        self.writer.write_u32::<BigEndian>(0).expect("Error while writing hash (0)!");
        Ok(())
    }
}
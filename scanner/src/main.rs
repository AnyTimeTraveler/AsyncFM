extern crate crc32fast;

use std::io::{BufWriter, Read, Write};
use std::fs::{read_dir, read_link, DirEntry, File, metadata};
use std::os::unix::fs::PermissionsExt;
use std::os::linux::fs::MetadataExt;
use std::path::{Path, PathBuf};
use byteorder::{BigEndian, WriteBytesExt};
use crc32fast::Hasher;
use std::env;
use std::sync::mpsc::{sync_channel, SyncSender, Receiver};
use std::thread::{spawn, sleep};
use std::time::Duration;

struct Progress {
    id: u64,
    name: String,
}

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() != 3 {
        println!("Usage: <target file> <source folder>");
        return;
    }

    println!("Source: {}\nTarget: {}", args[2], args[1]);

    let (log, rx) = sync_channel(0);


    // Spawn logger
    let logger = spawn(move || {
        let mut last_id = 0;
        let rx: Receiver<Progress> = rx;
        for p in rx {
            if last_id == p.id {
                print!(".");
            } else {
                print!("\nScanning... {:>9} : {:>6} items/s => {}", p.id, p.id - last_id, p.name);
                last_id = p.id;
            }
            sleep(Duration::from_millis(1000));
        }
        println!("\nDone!");
    });

    let output_file = File::create(&args[1]).expect("Can't open target file!");
    let mut output_file: BufWriter<File> = BufWriter::new(output_file);

    visit_dirs(0, Path::new(&args[2]), &mut output_file, &log);

    output_file.flush().expect("Error flushing target file!");

    drop(log);
    logger.join().expect("Error while waiting for the logger to finish!");
}


fn visit_dirs(parent_id: u64, dir: &Path, mut buf: &mut BufWriter<File>, log: &SyncSender<Progress>) -> u64 {
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

struct Meta {

}

fn read_file(id: &u64, parent_id: &u64, entry: &DirEntry, buf: &mut BufWriter<File>, log: &SyncSender<Progress>) {
    let name = entry.file_name();
    let name = name.to_str().unwrap();

    let _ = log.try_send(Progress { id: *id, name: name.to_string() });
    // TODO: Add megabyte counter with atomic

    let name = name.as_bytes();

    let mut flags: u8 = 0b00000000;
    let mut mode: Option<u32> = None;
    let mut uid: Option<u32> = None;
    let mut gid: Option<u32> = None;
    let mut size: Option<u64> = None;
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
    }

    let created = match meta.created() {
        Err(_) => 0,
        Ok(x) => x.elapsed().unwrap().as_secs(),
    };

    let modified = match meta.modified() {
        Err(_) => 0,
        Ok(x) => x.elapsed().unwrap().as_secs(),
    };

    let link_result = read_link(entry.path()).ok();
    let mut hash: Option<u32> = None;
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
    let hash = hash;

    write_entry(buf, id, parent_id, name, flags, meta.len(), created, modified, link_dest, mode, &hash);
}

fn write_entry(buf: &mut BufWriter<File>, id: &u64, parent_id: &u64, name: &[u8], flags: u8, size: u64, created: u64, modified: u64, link_dest: Option<PathBuf>, mode: Option<u32>, hash: &Option<u32>) {
    buf.write_u64::<BigEndian>(*id).expect("Error writing id!");
    buf.write_u64::<BigEndian>(*parent_id).expect("Error writing parent id!");
    buf.write_u64::<BigEndian>(name.len() as u64).expect("Error writing name length!");
    buf.write_all(name).expect("Error writing name!");
    buf.write_u64::<BigEndian>(size).expect("Error writing file size!");
    buf.write_u8(flags).expect("Error writing flags!");
    match mode {
        Some(data) => {
            buf.write_u32::<BigEndian>(data).expect("Error writing permissions!");
        }
        None => {
            buf.write_u32::<BigEndian>(0u32).expect("Error writing permissions (0)!");
        }
    }
    buf.write_u64::<BigEndian>(modified).expect("Error writing access time!");
    buf.write_u64::<BigEndian>(created).expect("Error writing creation time!");

    match link_dest {
        Some(data) => {
            let data = data.to_str().unwrap().as_bytes();
            buf.write_u64::<BigEndian>(data.len() as u64).expect("Error writing name length!");
            buf.write_all(data).expect("Error writing name!");
        }
        None => ()
    }

    match hash {
        Some(data) => {
            buf.write_u32::<BigEndian>(*data).expect("Error while writing hash!");
        }
        None => {
            buf.write_u32::<BigEndian>(0).expect("Error while writing hash (0)!");
        }
    }
}

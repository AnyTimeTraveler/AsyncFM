use std::fs;
use std::fs::OpenOptions;
use std::hash::Hasher;
use std::io::{self, BufReader, BufWriter, Read};
use std::path::Path;
use std::sync::mpsc::Sender;
use std::time::Instant;

use seahash::SeaHasher;

use crate::{FileFlags, FileMetadata, Header, HeaderFlags};
use crate::Log::{self, Error, Progress};

pub fn build_hashed_image(source_file: &Path, target_file: &Path, log: Sender<Log>) -> u64 {
    let input_file = OpenOptions::new()
        .read(true)
        .write(false)
        .create(false)
        .open(source_file)
        .expect("Can't open source file!");

    let output_file = OpenOptions::new()
        .read(false)
        .write(true)
        .create(true)
        .open(target_file)
        .expect("Can't open target file!");

    let mut ibuf = BufReader::new(input_file);
    let mut obuf = BufWriter::new(output_file);

    let mut header = Header::read(&mut ibuf);

    header.flags.insert(HeaderFlags::HAS_HASHES);

    header.write(&mut obuf);

    let base_path = String::from_utf8(header.base_path).expect("AAA");

    let mut files: Vec<FileMetadata> = Vec::with_capacity(header.entries as usize);

    for _ in 0..header.entries {
        files.push(FileMetadata::read(&mut ibuf));
    }

    let mut hashed_files_count = 0;

    for id in 0..header.entries {
        let mut file: FileMetadata = files[id as usize].clone();
        let path = get_path(&base_path, &files, &file);
        if file.flags == FileFlags::IS_FILE && file.hash.is_none() {
            match hash(&path, id, &log) {
                Ok(hash) => { file.hash = Some(hash); }
                Err(error) => { log.send(Error(format!("Error hashing file: {} : {:?}", path, error))).expect("Error logging error!"); }
            }

            hashed_files_count += 1;
        }
        file.write(&mut obuf);
    }

    hashed_files_count
}

fn get_path(base_path: &str, files: &[FileMetadata], file: &FileMetadata) -> String {
    let mut path = base_path.to_owned();
    let mut stack = vec![file.id];
    let mut current_file = file;

    while current_file.parent_id != 0 {
        stack.push(current_file.parent_id);
        current_file = files.get(current_file.parent_id as usize).expect("Error finding parent!");
    }

    stack.reverse();

    for item in stack {
        path += "/";
        path += files[item as usize].name.as_str();
    }

    path
}

pub fn hash(file: &str, id: u64, log: &Sender<Log>) -> Result<u64, io::Error> {
    log.send(Progress { id, path: file.to_string() }).expect("Error logging error!");
    let mut hasher = SeaHasher::new();
    let mut scanned_file = fs::File::open(&file)?;
    let mut buffer = [0u8; 1024 * 1024];

    let mut count;
    let mut start_time = Instant::now();
    loop {
        count = scanned_file.read(&mut buffer)?;
        hasher.write(&buffer[0..count]);
        if start_time.elapsed().as_secs() > 5 {
            log.send(Progress { id, path: file.to_string() }).expect("Error logging error!");
            start_time = Instant::now();
        }

        if count <= 0 {
            break;
        }
    }
    Ok(hasher.finish())
}
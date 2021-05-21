use crc32fast::Hasher;
use std::fs;
use std::io::{Read, Error, BufWriter, BufReader};
use std::time::Instant;
use crate::scanner::{Progress, Header, FileMetadata, write_header};
use std::sync::mpsc::{SyncSender, Sender};
use crate::Options;
use std::path::Path;
use std::fs::{OpenOptions, File};
use byteorder::{BigEndian, ReadBytesExt};

pub(crate) fn build_hashed_image(options: &Options, log: &Sender<Progress>, last_id: u64) {
    let directory_to_scan = Path::new(&options.source_folder);

    let input_file = OpenOptions::new()
        .read(true)
        .write(false)
        .create(false)
        .open(Path::new(&options.target_file))
        .expect("Can't open base file!");

    let output_file = OpenOptions::new()
        .read(false)
        .write(true)
        .create(true)
        .open(Path::new(&options.hash_file))
        .expect("Can't open hash file!");

    let mut ibuf = BufReader::new(input_file);
    let mut obuf = BufWriter::new(output_file);

    let header = Header {
        version: 1u32,
        flags: 1u8,
        entries: last_id,
        base_path: directory_to_scan.to_str().unwrap().as_bytes(),
    };

    write_header(&header, &mut obuf);

    let stack: Vec<String> = Vec::new();

    for i in 0..last_id {
        process_entry(&mut ibuf);
    }
}

fn read_string(buf: &mut BufReader<File>) -> Result<Vec<u8>, Error> {
    let mut byte = buf.read_u8()?;
    let mut bytes = Vec::new();
    while byte != 0u8 {
        bytes.push(byte);
        byte = buf.read_u8()?;
    }
    Result::Ok(bytes)
}

fn process_entry(buf: &mut BufReader<File>) -> FileMetadata {
    let id = buf.read_u64::<BigEndian>().expect("Error reading id!");
    let parent_id = buf.read_u64::<BigEndian>().expect("Error reading parent id!");
    let mut name = read_string(buf).expect("Error reading name!");
    let flags = buf.read_u8().expect("Error reading flags!");
    let mode = buf.read_u32::<BigEndian>().expect("Error reading mode!");
    let uid = buf.read_u32::<BigEndian>().expect("Error reading uid!");
    let gid = buf.read_u32::<BigEndian>().expect("Error reading gid!");
    let size = buf.read_u64::<BigEndian>().expect("Error reading size!");
    let created = buf.read_i64::<BigEndian>().expect("Error reading created!");
    let modified = buf.read_i64::<BigEndian>().expect("Error reading modified!");
    let accessed = buf.read_i64::<BigEndian>().expect("Error reading accessed!");

    let mut link_dest = read_string(buf).expect("Error reading name!");
    let hash = buf.read_u32::<BigEndian>().expect("Error while reading hash!");


    let file = FileMetadata {
        id,
        parent_id,
        name: name.as_slice(),
        flags,
        mode,
        uid,
        gid,
        size,
        created,
        modified,
        accessed,
        link_dest: link_dest.as_slice(),
        hash
    };


}

pub(crate) fn hash(file: String, id: u64, log: &SyncSender<Progress>) -> Result<u32, Error> {
    let _ = log.try_send(Progress { id, path: file.to_string() });
    let mut hasher = Hasher::new();
    let mut scanned_file = fs::File::open(&file)?;
    let mut buffer = [0u8; 1024];

    let mut count = scanned_file.read(&mut buffer[..])?;
    let mut start_time = Instant::now();
    while count > 0 {
        count = scanned_file.read(&mut buffer[..])?;
        hasher.update(&buffer[0..count]);
        if start_time.elapsed().as_secs() > 5 {
            let _ = log.try_send(Progress { id, path: file.to_string() });
            start_time = Instant::now();
        }
    }
    Result::Ok(hasher.finalize())
}
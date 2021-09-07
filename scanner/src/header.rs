use std::fs::File;
use std::io::{BufReader, BufWriter, Read, Seek, Write};
use std::io::SeekFrom::Start;
use std::path::PathBuf;

use byteorder::{BigEndian, ReadBytesExt, WriteBytesExt};

bitflags! {
    pub struct HeaderFlags: u8 {
        const HAS_HASHES = 0b00000001;
    }
}

#[derive(Debug)]
pub struct Header {
    pub version: u8,
    pub flags: HeaderFlags,
    pub entries: u64,
    pub base_path: Vec<u8>,
}

impl Header {
    pub fn new(base_path: &PathBuf) -> Header {
        Header {
            version: 1,
            flags: HeaderFlags::empty(),
            entries: 0,
            base_path: base_path.to_string_lossy().as_bytes().to_vec(),
        }
    }

    pub fn write(&self, buf: &mut BufWriter<File>) {
        buf.seek(Start(0)).expect("Error seeking in file!");
        buf.write_u8(self.version).expect("Error writing header!");
        buf.write_u8(self.flags.bits()).expect("Error writing flags!");
        buf.write_u64::<BigEndian>(self.entries).expect("Error writing entries!");
        buf.write_u32::<BigEndian>(self.base_path.len() as u32).expect("Error writing entries!");
        buf.write_all(&self.base_path).expect("Error writing base path!");
    }

    pub fn read(buf: &mut BufReader<File>) -> Header {
        buf.seek(Start(0)).expect("Error seeking in file!");
        let version = buf.read_u8().expect("Error reading version!");
        let flags = HeaderFlags::from_bits(buf.read_u8().expect("Error reading flags!")).expect("Invalid flags value!");
        let entries = buf.read_u64::<BigEndian>().expect("Error reading entries count!");
        let base_path_len = buf.read_u32::<BigEndian>().expect("Error reading base path length!");
        let mut base_path = vec![0u8; base_path_len as usize];
        buf.read_exact(&mut base_path).expect("Error reading base path!");

        Header {
            version,
            flags,
            entries,
            base_path,
        }
    }
}

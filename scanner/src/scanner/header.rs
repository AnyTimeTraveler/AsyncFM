use std::fs::File;
use std::io::{BufWriter, Seek, Write};
use std::io::SeekFrom::Start;
use std::path::PathBuf;

use byteorder::{BigEndian, WriteBytesExt};

bitflags! {
    pub(crate) struct Flags: u8 {
        const HAS_HASHES = 0b00000001;
    }
}

#[derive(Debug)]
pub(crate) struct Header {
    pub(crate) version: u8,
    pub(crate) flags: Flags,
    pub(crate) entries: u64,
    pub(crate) base_path: Vec<u8>,
}

impl Header {
    pub(crate) fn new(base_path: &PathBuf) -> Header {
        Header {
            version: 1,
            flags: Flags::empty(),
            entries: 0,
            base_path: base_path.to_string_lossy().as_bytes().to_vec(),
        }
    }

    pub(crate) fn write(&self, buf: &mut BufWriter<File>) {
        buf.seek(Start(0)).expect("Error seeking in file!");
        buf.write_u8(self.version).expect("Error writing header!");
        buf.write_u8(self.flags.bits()).expect("Error writing flags!");
        buf.write_u64::<BigEndian>(self.entries).expect("Error writing entries!");
        buf.write_u32::<BigEndian>(self.base_path.len() as u32).expect("Error writing entries!");
        buf.write_all(&self.base_path).expect("Error writing base path!");
    }
}

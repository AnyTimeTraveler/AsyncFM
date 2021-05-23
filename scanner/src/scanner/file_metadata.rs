use std::fs::File;
use std::io::{BufWriter, Write};

use byteorder::{BigEndian, WriteBytesExt};

bitflags! {
    pub(crate) struct FileFlags: u8 {
        const IS_SYMLINK = 0b00000001;
        const IS_FILE = 0b00000010;
        const IS_DIRECTORY = 0b00000100;
    }
}

#[derive(Debug)]
pub(crate) struct FileMetadata {
    pub(crate) id: u64,
    pub(crate) parent_id: u64,
    pub(crate) name: String,
    pub(crate) flags: FileFlags,
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
    pub(crate) link_dest: Option<String>,
    pub(crate) hash: Option<u32>,
}

impl FileMetadata {
    pub(crate) fn write_entry(&self, output: &mut BufWriter<File>) {
        output.write_u64::<BigEndian>(self.id).expect("Error writing id!");
        output.write_u64::<BigEndian>(self.parent_id).expect("Error writing parent id!");

        let name_bytes = self.name.as_bytes();
        output.write_u32::<BigEndian>(name_bytes.len() as u32).expect("Error writing length of the name string!");
        output.write_all(name_bytes).expect("Error writing name!");

        output.write_u8(self.flags.bits()).expect("Error writing flags!");
        output.write_u32::<BigEndian>(self.mode).expect("Error writing mode!");
        output.write_u32::<BigEndian>(self.uid).expect("Error writing uid!");
        output.write_u32::<BigEndian>(self.gid).expect("Error writing gid!");
        output.write_u64::<BigEndian>(self.size).expect("Error writing size!");
        output.write_i64::<BigEndian>(self.created).expect("Error writing created!");
        output.write_i64::<BigEndian>(self.modified).expect("Error writing modified!");
        output.write_i64::<BigEndian>(self.accessed).expect("Error writing accessed!");

        if let Some(link_dest) = &self.link_dest {
            let link_bytes = link_dest.as_bytes();
            output.write_u32::<BigEndian>(link_bytes.len() as u32).expect("Error writing length of the link dest string!");
            output.write_all(link_bytes).expect("Error writing link dest!");
        } else {
            output.write_u32::<BigEndian>(0).expect("Error writing link dest position!");
        }

        output.write_u32::<BigEndian>(self.hash.unwrap_or(0)).expect("Error while writing hash!");
    }
}

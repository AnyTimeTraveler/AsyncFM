use std::fs::File;
use std::io::{BufWriter, Seek, Write};

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
    pub(crate) fn write_entry(&self, entries_out: &mut BufWriter<File>, strings_out: &mut BufWriter<File>) {
        entries_out.write_u64::<BigEndian>(self.id).expect("Error writing id!");
        entries_out.write_u64::<BigEndian>(self.parent_id).expect("Error writing parent id!");

        let name_pos: u64 = strings_out.stream_position().expect("Error retrieving stream position!");
        strings_out.write_all(self.name.as_bytes()).expect("Error writing name!");
        entries_out.write_u64::<BigEndian>(name_pos).expect("Error writing name position!");
        entries_out.write_u64::<BigEndian>(self.name.as_bytes().len() as u64).expect("Error writing length of the name string!");

        entries_out.write_u8(self.flags.bits()).expect("Error writing flags!");
        entries_out.write_u32::<BigEndian>(self.mode).expect("Error writing mode!");
        entries_out.write_u32::<BigEndian>(self.uid).expect("Error writing uid!");
        entries_out.write_u32::<BigEndian>(self.gid).expect("Error writing gid!");
        entries_out.write_u64::<BigEndian>(self.size).expect("Error writing size!");
        entries_out.write_i64::<BigEndian>(self.created).expect("Error writing created!");
        entries_out.write_i64::<BigEndian>(self.modified).expect("Error writing modified!");
        entries_out.write_i64::<BigEndian>(self.accessed).expect("Error writing accessed!");

        if let Some(link_dest) = &self.link_dest {
            let link_dest_pos: u64 = strings_out.stream_position().expect("Error retrieving stream position!");
            strings_out.write_all(link_dest.as_bytes()).expect("Error writing link dest!");
            entries_out.write_u64::<BigEndian>(link_dest_pos).expect("Error writing link dest position!");
            entries_out.write_u64::<BigEndian>(link_dest.as_bytes().len() as u64).expect("Error writing length of the link dest string!");
        } else {
            entries_out.write_u64::<BigEndian>(0).expect("Error writing link dest position!");
            entries_out.write_u64::<BigEndian>(0).expect("Error writing length of the link dest string!");
        }

        entries_out.write_u32::<BigEndian>(self.hash.unwrap_or(0)).expect("Error while writing hash!");
    }
}

use std::fs::File;
use std::io::{BufReader, BufWriter, Read, Write};

use byteorder::{BigEndian, ReadBytesExt, WriteBytesExt};

bitflags! {
    pub struct FileFlags: u8 {
        const IS_SYMLINK = 0b00000001;
        const IS_FILE = 0b00000010;
        const IS_DIRECTORY = 0b00000100;
    }
}

#[derive(Debug, Clone)]
pub struct FileMetadata {
    pub id: u64,
    pub parent_id: u64,
    pub name: String,
    pub flags: FileFlags,
    /**
    Mode: standard linux mode data.
     */
    pub mode: u32,
    pub uid: u32,
    pub gid: u32,
    pub size: u64,
    pub created: i64,
    pub modified: i64,
    pub accessed: i64,
    pub link_dest: Option<String>,
    pub hash: Option<u32>,
}

impl FileMetadata {
    pub fn write(&self, buf: &mut BufWriter<File>) {
        buf.write_u64::<BigEndian>(self.id).expect("Error writing id!");
        buf.write_u64::<BigEndian>(self.parent_id).expect("Error writing parent id!");

        let name_bytes = self.name.as_bytes();
        buf.write_u32::<BigEndian>(name_bytes.len() as u32).expect("Error writing length of the name string!");
        buf.write_all(name_bytes).expect("Error writing name!");

        buf.write_u8(self.flags.bits()).expect("Error writing flags!");
        buf.write_u32::<BigEndian>(self.mode).expect("Error writing mode!");
        buf.write_u32::<BigEndian>(self.uid).expect("Error writing uid!");
        buf.write_u32::<BigEndian>(self.gid).expect("Error writing gid!");
        buf.write_u64::<BigEndian>(self.size).expect("Error writing size!");
        buf.write_i64::<BigEndian>(self.created).expect("Error writing created!");
        buf.write_i64::<BigEndian>(self.modified).expect("Error writing modified!");
        buf.write_i64::<BigEndian>(self.accessed).expect("Error writing accessed!");

        if let Some(link_dest) = &self.link_dest {
            let link_bytes = link_dest.as_bytes();
            buf.write_u32::<BigEndian>(link_bytes.len() as u32).expect("Error writing length of the link dest string!");
            buf.write_all(link_bytes).expect("Error writing link dest!");
        } else {
            buf.write_u32::<BigEndian>(0).expect("Error writing link dest position!");
        }

        buf.write_u32::<BigEndian>(self.hash.unwrap_or(0)).expect("Error while writing hash!");
    }

    pub fn read(buf: &mut BufReader<File>) -> FileMetadata {
        let id = buf.read_u64::<BigEndian>().expect("Error reading id!");
        let parent_id = buf.read_u64::<BigEndian>().expect("Error reading parent id!");

        let name_bytes_len = buf.read_u32::<BigEndian>().expect("Error reading length of the name string!");
        let mut name = vec![0u8; name_bytes_len as usize];
        buf.read_exact(&mut name).expect("Error reading name!");
        let name = String::from_utf8(name).expect("Error parsing utf-8 bytes for name!");

        let flags = FileFlags::from_bits(buf.read_u8().expect("Error reading flags!")).expect("Error reading flags!");
        let mode = buf.read_u32::<BigEndian>().expect("Error reading mode!");
        let uid = buf.read_u32::<BigEndian>().expect("Error reading uid!");
        let gid = buf.read_u32::<BigEndian>().expect("Error reading gid!");
        let size = buf.read_u64::<BigEndian>().expect("Error reading size!");
        let created = buf.read_i64::<BigEndian>().expect("Error reading created!");
        let modified = buf.read_i64::<BigEndian>().expect("Error reading modified!");
        let accessed = buf.read_i64::<BigEndian>().expect("Error reading accessed!");

        let link_dest_len = buf.read_u32::<BigEndian>().expect("Error reading length of the link dest string!");
        let link_dest = if link_dest_len > 0 {
            let mut link_dest_bytes = vec![0u8; link_dest_len as usize];
            buf.read_exact(&mut link_dest_bytes).expect("Error reading link dest!");
            Some(String::from_utf8(link_dest_bytes).expect("Error parsing utf-8 bytes for link dest!"))
        } else {
            None
        };

        let hash = buf.read_u32::<BigEndian>().expect("Error while reading hash!");
        let hash = if hash != 0 {
            Some(hash)
        } else {
            None
        };

        FileMetadata {
            id,
            parent_id,
            name,
            flags,
            mode,
            uid,
            gid,
            size,
            created,
            modified,
            accessed,
            link_dest,
            hash,
        }
    }
}

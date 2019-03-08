use std::io::{self, BufWriter, Write};
use std::fs::{self, DirEntry, File};
use std::path::Path;
use byteorder::{BigEndian, WriteBytesExt};


// one possible implementation of walking a directory only visiting files
fn visit_dirs(dir: &Path, cb: &FnMut(&DirEntry)) -> io::Result<()> {
//    for each entry in directory
    for entry in fs::read_dir(dir)? {
//        return error if entry has issues
        let entry = entry?;
        let path = entry.path();
        if path.is_dir() {
            visit_dirs(&path, cb)?;
        } else {
            cb(&entry);
        }
    }
    Ok(())
}

fn write<T: WriteBytesExt>(file: &mut T, dir: &DirEntry) {
    let name = dir.file_name().to_str().unwrap();
    println!("Writing {}", name);

    let name = name.as_bytes();
    let mut flags: u8 = 0;
    let meta = dir.metadata().unwrap();
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

    file.write_u64::<BigEndian>(name.len() as u64);
    file.write_all(name);
    file.write_u64::<BigEndian>(meta.len());
    file.write_u8(flags);
    file.write_u64::<BigEndian>(meta.accessed().unwrap().elapsed().unwrap().as_secs());
    file.write_u64::<BigEndian>(meta.created().unwrap().elapsed().unwrap().as_secs());
}

fn main() {
    let mut buf = BufWriter::new(File::create("/home/simon/").expect("Problems with output file."));

    let cb = |dir: &DirEntry| {
        write(&mut buf, dir);
    };

    visit_dirs(Path::new("/home/simon/"), &cb);

    buf.flush();
}

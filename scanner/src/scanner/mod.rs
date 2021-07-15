use std::fs::{self, File, OpenOptions};
use std::io::{BufWriter, Write};
use std::os::linux::fs::MetadataExt;
use std::path::Path;
use std::path::PathBuf;
use std::sync::mpsc::Sender;

use crate::file_metadata::{FileFlags, FileMetadata};
use crate::header::Header;
use crate::Log;
use crate::scanner::Log::{Error, Progress};

pub struct Scanner {
    root_path: PathBuf,
    log: Sender<Log>,
    entry_read_counter: u64,
    output: BufWriter<File>,
}

impl Scanner {
    pub fn new(source_folder: &Path, target_file: &Path, log: Sender<Log>) -> Scanner {
        let output_file = OpenOptions::new()
            .read(false)
            .write(true)
            .create(true)
            .open(target_file)
            .expect("Can't open target combined file!");

        Scanner {
            root_path: source_folder.to_path_buf(),
            log,
            entry_read_counter: 0,
            output: BufWriter::new(output_file),
        }
    }

    pub fn scan(&mut self) {
        let mut header = Header::new(&self.root_path);

        header.write(&mut self.output);

        self.visit_file(0, &self.root_path.clone());
        self.output.flush().expect("Error flushing target file!");

        header.entries = self.entry_read_counter;

        header.write(&mut self.output);

        self.output.flush().expect("Error flushing target file!");
    }

    fn visit_file(&mut self, parent_id: u64, dir: &Path) {
        let id = self.next_id();
        self.read_file(id, parent_id, dir);

        match dir.symlink_metadata() {
            Err(error) => self.log.send(Error(format!("Couldn't determine file type of: {:?} {:?}", dir, error))).expect("Error logging error!"),
            Ok(meta) => {
                if meta.is_dir() && !Scanner::is_symlink(dir) {
                    self.visit_dir(id, dir);
                }
            }
        }
    }

    fn is_symlink(path: &Path) -> bool {
        path.read_link().is_ok()
    }

    fn visit_dir(&mut self, parent_id: u64, dir: &Path) {
        match dir.read_dir() {
            Err(error) => {
                self.log.send(Error(format!("Error reading file: {} : {:?}", dir.to_string_lossy(), error))).expect("Error logging error!");
                return;
            }
            Ok(dir_info) => {
                for entry in dir_info {
                    match entry {
                        Err(error) => self.log.send(Error(format!("Couldn't read entry: {:?}", error))).expect("Error logging error!"),
                        Ok(entry) => {
                            self.visit_file(parent_id, &entry.path());
                        }
                    }
                }
            }
        }
    }

    fn next_id(&mut self) -> u64 {
        self.entry_read_counter += 1;
        self.entry_read_counter - 1
    }

    fn read_file(&mut self, id: u64, parent_id: u64, path: &Path) {
        let buf = path.to_path_buf();
        let name = buf.file_name().expect("Error reading filename!").to_string_lossy();

        self.log.send(Progress { id, path: buf.to_string_lossy().to_string() }).expect("Error logging error!");

        let file = match path.metadata() {
            Err(_) => FileMetadata {
                id,
                parent_id,
                name: name.to_string(),
                mode: 0,
                uid: 0,
                gid: 0,
                size: 0,
                created: 0,
                modified: 0,
                accessed: 0,
                link_dest: None,
                hash: None,
                flags: FileFlags::empty(),
            },
            Ok(meta) => {
                let file_type = meta.file_type();

                let mut flags = FileFlags::empty();
                if file_type.is_file() { flags |= FileFlags::IS_FILE }
                if file_type.is_dir() { flags |= FileFlags::IS_DIRECTORY }
                if file_type.is_symlink() { flags |= FileFlags::IS_SYMLINK }

                FileMetadata {
                    id,
                    parent_id,
                    name: name.to_string(),
                    mode: meta.st_mode(),
                    uid: meta.st_uid(),
                    gid: meta.st_gid(),
                    size: meta.st_size(),
                    created: meta.st_ctime(),
                    modified: meta.st_mtime(),
                    accessed: meta.st_atime(),
                    link_dest: fs::read_link(path)
                        .map(|dest| dest.to_string_lossy().to_string())
                        .ok(),
                    hash: None,
                    flags,
                }
            }
        };

        file.write(&mut self.output);
    }

    pub fn amount_entries_read(&self) -> u64 {
        self.entry_read_counter
    }
}

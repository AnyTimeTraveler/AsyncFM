use std::path::PathBuf;

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
}

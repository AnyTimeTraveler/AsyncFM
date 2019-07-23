use crc32fast::Hasher;

pub fn hash(){
    let mut hasher = Hasher::new();
    let mut scanned_file = fs::File::open(entry.path())?;
    let mut buffer = [0u8; 1024];

    let mut count = scanned_file.read(&mut buffer[..])?;
    let mut start_time = Instant::now();
    while count > 0 {
        count = scanned_file.read(&mut buffer[..])?;
        hasher.update(&buffer[0..count]);
        if start_time.elapsed().as_secs() > 5 {
            let _ = log.try_send(Progress { id: *id, name: path.to_string() });
            start_time = Instant::now();
        }
    }
    hash = hasher.finalize();
}
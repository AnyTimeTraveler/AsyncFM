extern crate crc32fast;

mod scanner;

#[cfg(windows)]
mod windows_scanner;
#[cfg(unix)]
mod linux_scanner;


use std::io::{BufWriter, Write};
use std::path::{Path, PathBuf};
use crc32fast::Hasher;
use std::env;
use std::sync::mpsc::{sync_channel, SyncSender, Receiver};
use std::thread::{spawn, sleep};
use std::time::Duration;
use std::fs::File;
use scanner::Progress;

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() != 3 {
        println!("Usage: <target file> <source folder>");
        return;
    }

    println!("Source: {}\nTarget: {}", args[2], args[1]);

    let (log, rx) = sync_channel(0);


    // Spawn logger
    let logger = spawn(move || {
        let mut last_id = 0;
        let rx: Receiver<Progress> = rx;
        for p in rx {
            if last_id == p.id {
                print!(".");
            } else {
                print!("\nScanning... {:>9} : {:>6} items/s => {}", p.id, p.id - last_id, p.name);
                last_id = p.id;
            }
            sleep(Duration::from_millis(250));
        }
        println!("\nDone!");
    });

    let output_file = File::create(&args[1]).expect("Can't open target file!");
    let mut output_file: BufWriter<File> = BufWriter::new(output_file);

    #[cfg(unix)]
        let read_file = linux_scanner::read_file;
    #[cfg(windows)]
        let read_file = windows_scanner::read_file;

    scanner::visit_dirs(0, Path::new(&args[2]), &mut output_file, &log, read_file);

    output_file.flush().expect("Error flushing target file!");

    drop(log);
    logger.join().expect("Error while waiting for the logger to finish!");
}

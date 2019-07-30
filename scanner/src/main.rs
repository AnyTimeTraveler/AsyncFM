extern crate crc32fast;

use std::env;
use std::path::Path;
use std::sync::mpsc::{Receiver, sync_channel};
use std::thread::{sleep, spawn};
use std::time::Duration;

use crate::scanner::Progress;

mod scanner;

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
    });

    let last_id = scanner::scan_directory(Path::new(&args[2]),Path::new(&args[1]), &log);

    drop(log);
    logger.join().expect("Error while waiting for the logger to finish!");
    println!("\n\nDone!\nScanned {} files in total!", last_id);
}

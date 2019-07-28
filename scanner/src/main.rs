extern crate crc32fast;

use std::env;
use std::fs::File;
use std::io::{BufWriter, Write};
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

    let output_file = File::create(&args[1]).expect("Can't open target file!");
    let mut output_file: BufWriter<File> = BufWriter::new(output_file);

    let last_id = scanner::scan_directory(Path::new(&args[2]), &mut output_file, &log);


    output_file.flush().expect("Error flushing target file!");

    drop(log);
    logger.join().expect("Error while waiting for the logger to finish!");
    println!("\n\nDone!\nScanned {} files in total!", last_id);
}

extern crate argparse;

use std::sync::mpsc::{Receiver, sync_channel};
use std::thread::{sleep, spawn};
use std::time::Duration;

use argparse::{ArgumentParser, StoreTrue, Store};

use crate::scanner::Progress;

mod scanner;
mod hasher;

pub(crate) struct Options {
    pub silent: bool,
    pub follow_symlinks: bool,
    pub target_file: String,
    pub hash_file: String,
    pub source_folder: String,
    pub lograte: u64,
}

fn main() {
    let mut options = Options {
        silent: false,
        follow_symlinks: true,
        target_file: "".to_string(),
        hash_file: "".to_string(),
        source_folder: "".to_string(),
        lograte: 250,
    };

    {  // this block limits scope of borrows by ap.refer() method
        let mut ap = ArgumentParser::new();
        ap.set_description("Create an image of a filesystem to manipulate later");
        ap.refer(&mut options.silent).add_option(&["-s", "--silent"], StoreTrue, "Do not report the current status");
        ap.refer(&mut options.follow_symlinks).add_option(&["-f", "--follow-symlinks"], StoreTrue, "Follow symlinks");
        ap.refer(&mut options.lograte).add_option(&["-l", "--lograte"], Store, "Output a log entry every x milliseconds. (Default: 250)");
        ap.refer(&mut options.source_folder).add_argument("source", Store, "Folder to scan").required();
        ap.refer(&mut options.target_file).add_argument("target", Store, "Path of the image file to write").required();
        ap.refer(&mut options.hash_file).add_argument("hash", Store, "If specified, the program will generate a second file, which contains the same image, but with the hash for each file to allow better deduplication (may take significantly longer)");
        ap.parse_args_or_exit();
    }

    println!("Source: {}\nTarget: {}", options.source_folder, options.target_file);

    let (log, rx) = sync_channel(0);

    let lograte = options.lograte;
    // Spawn logger
    let logger = spawn(move || {
        let mut last_id = 0;
        let mut action = "Scanning";
        let rx: Receiver<Progress> = rx;
        for p in rx {
            if p.id < last_id {
                action = "Hashing";
            }
            if last_id == p.id {
                print!(".");
            } else {
                print!("\n{}... {:>9} : {:>6} items/s => {}", action, p.id, (p.id - last_id) * 1000 / lograte, p.name);
                last_id = p.id;
            }
            sleep(Duration::from_millis(lograte));
        }
    });

    let last_id = scanner::scan_directory(&options, &log);

    if !options.hash_file.is_empty() {
        hasher::build_hashed_image(&options, &log, last_id);
    }
    drop(log);
    logger.join().expect("Error while waiting for the logger to finish!");
    println!("\n\nDone!\nScanned {} files in total!", last_id);
}

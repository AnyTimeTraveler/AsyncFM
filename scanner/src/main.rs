extern crate argparse;
#[macro_use]
extern crate bitflags;

use std::path::Path;
use std::sync::mpsc::{channel, Receiver};
use std::thread::{JoinHandle, spawn};
use std::time::{Duration, SystemTime};

use ansi_escapes::{CursorLeft, CursorPrevLine, CursorRestorePosition, CursorSavePosition, EraseLine};
// use ansi_escapes::{CursorHide, CursorShow};
use argparse::{ArgumentParser, Store, StoreTrue};
use num_format::{Locale, ToFormattedString};

pub use file_metadata::{FileFlags, FileMetadata};
pub use header::{Header, HeaderFlags};

use crate::hasher::build_hashed_image;
use crate::scanner::Scanner;

mod scanner;
mod hasher;
mod header;
mod file_metadata;

pub struct Options {
    pub silent: bool,
    pub follow_symlinks: bool,
    pub target_file: String,
    pub hash_file: String,
    pub source_folder: String,
    pub log_rate: u64,
}

pub enum Log {
    Progress {
        id: u64,
        path: String,
    },
    Error(String),
}

fn main() {
    let mut options = Options {
        silent: false,
        follow_symlinks: true,
        target_file: "".to_string(),
        hash_file: "".to_string(),
        source_folder: "".to_string(),
        log_rate: 100,
    };

    {  // this block limits scope of borrows by ap.refer() method
        let mut ap = ArgumentParser::new();
        ap.set_description("Create an image of a filesystem to manipulate later");
        ap.refer(&mut options.silent).add_option(&["-s", "--silent"], StoreTrue, "Do not report the current status");
        ap.refer(&mut options.follow_symlinks).add_option(&["-f", "--follow-symlinks"], StoreTrue, "Follow symlinks");
        ap.refer(&mut options.log_rate).add_option(&["-l", "--log-rate"], Store, "Output a log entry every x milliseconds. (Default: 100)");
        ap.refer(&mut options.source_folder).add_argument("source", Store, "Folder to scan").required();
        ap.refer(&mut options.target_file).add_argument("target", Store, "Path of the image file to write, without extension").required();
        ap.refer(&mut options.hash_file).add_argument("hash", Store, "If specified, the program will generate a second file, which contains the same image, but with the hash for each file to allow better deduplication (may take significantly longer)");
        ap.parse_args_or_exit();
    }

    println!("Source: {}\nTarget: {}\n", options.source_folder, options.target_file);

    // eprint!("{}", CursorHide);

    scan_folder(options.log_rate, &options.source_folder, &options.target_file);

    if !options.hash_file.is_empty() {
        println!("\n\nTarget: {}\nHashed: {}\n", options.target_file, options.hash_file);
        hash_folder(options.log_rate, &options.target_file, &options.hash_file);
    }

    // eprint!("{}", CursorShow);
}

fn scan_folder(log_rate: u64, source_folder: &str, target_file: &str) {
    let (tx, rx) = channel();
    let logger = spawn_logger("Scanning", rx, log_rate);

    let amount_entries_read = {
        let mut scanner = Scanner::new(Path::new(source_folder.trim_end_matches("/")), Path::new(target_file), tx);
        scanner.scan();
        scanner.amount_entries_read()
    };

    println!("\n\nDone!\nScanned {} files in total!", amount_entries_read.to_formatted_string(&Locale::de));
    logger.join().expect("Error while waiting for the logger to finish!");
}

fn hash_folder(log_rate: u64, unhashed_target_file: &str, hash_target_file: &str) {
    let (tx, rx) = channel();
    let logger = spawn_logger("Hashing", rx, log_rate);

    let amount_entries_hashed = build_hashed_image(Path::new(unhashed_target_file), Path::new(hash_target_file), tx);

    println!("\n\nDone!\nHashed {} files in total!", amount_entries_hashed.to_formatted_string(&Locale::de));
    logger.join().expect("Error while waiting for the logger to finish!");
}


fn spawn_logger(action: &'static str, rx: Receiver<Log>, log_rate: u64) -> JoinHandle<()> {
    let logger = spawn(move || {
        let mut count = 0;
        let mut time = SystemTime::now();
        eprint!("{}{}", CursorLeft, CursorSavePosition);
        for log in rx {
            match log {
                Log::Progress { id, path } => {
                    if time.elapsed().unwrap() > Duration::from_millis(log_rate) {
                        time = SystemTime::now();
                        eprint!("{}{}{}... {:>12} : {:>9} items/s => {}", EraseLine, CursorRestorePosition, action, id.to_formatted_string(&Locale::de), (count * 1000 / log_rate).to_formatted_string(&Locale::de), path);
                        count = 0;
                    } else {
                        count += 1;
                    }
                }
                Log::Error(error) => {
                    eprint!("\n{}{}{}{}", CursorPrevLine, EraseLine, CursorPrevLine, CursorLeft);
                    println!("{}\n", error);
                    eprint!("{}", CursorSavePosition);
                }
            }
        }
    });
    logger
}

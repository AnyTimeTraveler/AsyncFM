extern crate argparse;
#[macro_use]
extern crate bitflags;

use std::sync::mpsc::{channel, Receiver};
use std::thread::spawn;
use std::time::{Duration, SystemTime};

use ansi_escapes;
use ansi_escapes::{CursorHide, CursorLeft, CursorPrevLine, CursorShow, EraseLine};
use argparse::{ArgumentParser, Store, StoreTrue};
use num_format::{Locale, ToFormattedString};

use crate::scanner::{Log, Scanner};

mod scanner;
// mod hasher;

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
        lograte: 100,
    };

    {  // this block limits scope of borrows by ap.refer() method
        let mut ap = ArgumentParser::new();
        ap.set_description("Create an image of a filesystem to manipulate later");
        ap.refer(&mut options.silent).add_option(&["-s", "--silent"], StoreTrue, "Do not report the current status");
        ap.refer(&mut options.follow_symlinks).add_option(&["-f", "--follow-symlinks"], StoreTrue, "Follow symlinks");
        ap.refer(&mut options.lograte).add_option(&["-l", "--lograte"], Store, "Output a log entry every x milliseconds. (Default: 250)");
        ap.refer(&mut options.source_folder).add_argument("source", Store, "Folder to scan").required();
        ap.refer(&mut options.target_file).add_argument("target", Store, "Path of the image file to write, without extension").required();
        ap.refer(&mut options.hash_file).add_argument("hash", Store, "If specified, the program will generate a second file, which contains the same image, but with the hash for each file to allow better deduplication (may take significantly longer)");
        ap.parse_args_or_exit();
    }

    println!("Source: {}\nTarget: {}\n", options.source_folder, options.target_file);

    eprint!("{}", CursorHide);

    let (log, rx) = channel();

    let lograte = options.lograte;
    // Spawn logger
    let logger = spawn(move || {
        let mut count = 0;
        let rx: Receiver<Log> = rx;
        let mut time = SystemTime::now();
        for log in rx {
            match log {
                Log::Progress { id, path } => {
                    if time.elapsed().unwrap() > Duration::from_millis(lograte) {
                        time = SystemTime::now();
                        eprint!("{}{}Scanning... {:>12} : {:>9} items/s => {}", EraseLine, CursorLeft, id.to_formatted_string(&Locale::de), (count * 1000 / lograte).to_formatted_string(&Locale::de), path);
                        count = 0;
                    } else {
                        count += 1;
                    }
                }
                Log::Error(error) => {
                    eprint!("\n{}{}{}{}{}\n\n", CursorPrevLine, EraseLine, CursorPrevLine, CursorLeft, error)
                }
            }
        }
    });
    let amount_entries_read = {
        let mut scanner = Scanner::new(options, log);
        scanner.scan();
        scanner.amount_entries_read()
    };

    // if !options.hash_file.is_empty() {
    //     hasher::build_hashed_image(&options, &log, last_id);
    // }
    logger.join().expect("Error while waiting for the logger to finish!");
    println!("\n\nDone!\nScanned {} files in total!", amount_entries_read.to_formatted_string(&Locale::de));

    eprint!("{}", CursorShow);
}

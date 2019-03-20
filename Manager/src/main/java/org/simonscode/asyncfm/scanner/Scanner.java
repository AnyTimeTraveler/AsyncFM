package org.simonscode.asyncfm.scanner;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Scanner {

    public static final AtomicReference<String> FileString = new AtomicReference<>();
    public static final AtomicInteger FileCounter = new AtomicInteger(0);
    private static final Timer timer = new Timer();
    private static Thread mainThread;

    public static void main(String[] args) throws IOException {
        mainThread = Thread.currentThread();
        if (args.length < 2) {
            System.err.println("Arguments: <output file> [paths to scan...]");
            return;
        }
        // Some fancy logging every half second.
        timer.scheduleAtFixedRate(new TimerTask() {
            private int last;
            private boolean newline;

            @Override
            public void run() {
                if (!mainThread.isAlive()) {
                    System.out.println("The program has crashed. See stderr for more info.");
                    timer.cancel();
                }
                if (last == FileCounter.get()) {
                    System.out.print('.');
                    newline = true;
                    return;
                }
                if (newline)
                    System.out.println();
                last = FileCounter.get();
                System.out.printf("%tT > %d : %s%n", new Date(), FileCounter.get(), FileString);
                newline = false;
            }
        }, 500, 500);


        for (int i = 1; i < args.length; i++) {
            String path = args[i];
            System.out.println("Reading " + path);
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("Path not found: " + path);
                break;
            }
            System.out.printf("Writing to \"%s\"...%n", args[0]);
            try (FileOutputStream fos = new FileOutputStream(args[0])) {
                FileSystemWalker fsw = new FileSystemWalker(fos);
//                fsw.walk(path);
            }

            timer.cancel();
            System.out.printf("Done! Read %d objects.%n", FileCounter.get());
        }
    }
}

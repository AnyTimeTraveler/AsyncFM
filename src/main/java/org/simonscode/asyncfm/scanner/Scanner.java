package org.simonscode.asyncfm.scanner;


import org.simonscode.asyncfm.common.RootNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Scanner {

    public static final AtomicReference<String> FileString = new AtomicReference<>();
    public static final AtomicInteger FileCounter = new AtomicInteger(0);
    private static final Timer timer = new Timer();

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("This program requires at least one path to scanForChildren.");
            System.exit(0);
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            private int last;

            @Override
            public void run() {
                if (last == FileCounter.get())
                    return;
                last = FileCounter.get();
                System.out.printf("Current progress: %d : %s%n", FileCounter.get(), FileString);
            }
        }, 200, 200);
        for (String path : args) {
            System.out.println("Reading " + path);
            File file = new File(path);
            if (!file.exists()) {
                System.err.println("Path not found: " + path);
                break;
            }
            RootNode rootNode = new RootNode(file);
            rootNode.scanForChildren(file);

            timer.cancel();
            System.out.printf("Done! Read %d objects.%n", FileCounter.get());

            System.out.print("Writing now...");

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("result.struct"))) {
                oos.writeObject(rootNode);
            }
            System.out.println("Done!");
        }
    }
}

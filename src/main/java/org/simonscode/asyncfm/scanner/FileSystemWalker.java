package org.simonscode.asyncfm.scanner;

import org.simonscode.asyncfm.common.Hasher;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicLong;

public class FileSystemWalker {

    private final DataOutputStream dos;
    private final AtomicLong fileCounter;

    public FileSystemWalker(OutputStream os) {
        this.dos = new DataOutputStream(os);
        this.fileCounter = new AtomicLong();
    }


    public void walk(String path) throws IOException {
        File root = new File(path);
        recursiveWalk(writeFile(0L, root, root.getAbsolutePath()), root);
    }

    private void recursiveWalk(long id, File file) throws IOException {
        File[] files = file.listFiles();
        if (files == null) {
            System.err.println("Could not list files for path: " + file.getAbsolutePath());
            return;
        }

        for (File child : files) {
            if (!child.exists() || Files.isSymbolicLink(child.toPath()))
                continue;
            if (child.isDirectory()) {
                recursiveWalk(writeFile(id, child), child);
            } else if (Files.isRegularFile(child.toPath())) {
                Scanner.FileCounter.incrementAndGet();
                Scanner.FileString.set(child.getAbsolutePath());
                writeFile(id, child);
            }
        }
    }

    private long writeFile(long parentId, File file) throws IOException {
        return writeFile(parentId, file, file.getName());
    }

    private long writeFile(long parentId, File file, String name) throws IOException {
        byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));
        dos.writeLong(parentId);
        dos.writeInt(nameBytes.length);
        dos.write(nameBytes);
        dos.writeBoolean(!file.isDirectory());
        if (!file.isDirectory()) {
            dos.writeLong(file.length());
            dos.writeLong(Hasher.hash(file));
        }
        return fileCounter.incrementAndGet();
    }
}

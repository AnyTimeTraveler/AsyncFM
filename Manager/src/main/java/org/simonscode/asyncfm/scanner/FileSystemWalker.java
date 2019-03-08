package org.simonscode.asyncfm.scanner;

import org.simonscode.asyncfm.common.Hasher;
import org.simonscode.asyncfm.common.Node;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

public class FileSystemWalker {

    private final DataOutputStream dos;
    private final AtomicLong fileCounter;
    private final Node root;
    private String splitRegex = Matcher.quoteReplacement(System.getProperty("file.separator"));

    public FileSystemWalker(OutputStream os) {
        this.dos = new DataOutputStream(os);
        this.fileCounter = new AtomicLong();
        this.root = new Node(null, "");
    }


    public void walk(File rootFile) throws IOException {
        Files.walkFileTree(rootFile.toPath(), Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                store(dir, attrs);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                store(file, attrs);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                store(file, exc);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private Node getCurrent(String pathString) {
        String[] path = pathString.split(splitRegex);
        Node current = root;
        for (String name : path) {
            Node node = current.getChildByName(name);
            if (node != null) {
                current = node;
                continue;
            }
            node = new Node(current, name);
            current.addChild(node);
        }
        return current;
    }

    private void store(Path dir, IOException exc) {
        System.out.println(exc.getMessage());
    }

    private void store(Path file, BasicFileAttributes attrs) {
        Scanner.FileCounter.incrementAndGet();
        String pathString = file.toString();
        Scanner.FileString.set(pathString);
        Node current = getCurrent(pathString);
        current.setDirectory(attrs.isDirectory());
        current.setSize(attrs.size());
        if (attrs.isRegularFile()) {
            current.setHash(Hasher.hash(file.toFile()));
        }
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
//                recursiveWalk(writeFile(id, child), child);
            } else if (Files.isRegularFile(child.toPath())) {
                Scanner.FileCounter.incrementAndGet();
                Scanner.FileString.set(child.getAbsolutePath());
                writeFile(id, child);
            }
        }
    }

    private Node writeFile(long parentId, File file) throws IOException {
        return writeFile(parentId, file, file.getName());
    }

    private Node writeFile(long parentId, File file, String name) throws IOException {
        byte[] nameBytes = name.getBytes(Charset.forName("UTF-8"));
        dos.writeLong(parentId);
        dos.writeInt(nameBytes.length);
        dos.write(nameBytes);
        dos.writeBoolean(!file.isDirectory());
        if (!file.isDirectory()) {
            dos.writeLong(file.length());
            dos.writeLong(Hasher.hash(file));
        }
        return null;
    }
}

package org.simonscode.asyncfm.scanner;

import org.simonscode.asyncfm.common.Hasher;
import org.simonscode.asyncfm.common.Node;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

public class FileSystemWalker {

    private final DataOutputStream dos;
    private final AtomicLong fileCounter;
    private final HashMap<Path, Long> pathMap;
    private final Node root;
    private String splitRegex;

    public FileSystemWalker(OutputStream os) {
        this.dos = new DataOutputStream(os);
        this.fileCounter = new AtomicLong();
        this.pathMap = new HashMap<>();
        this.root = new Node(null, "");
    }


    public void walk(File rootFile) throws IOException {
        writeFile(0L, rootFile, rootFile.getAbsolutePath());
//
//        try (FileTreeWalker walker = new FileTreeWalker(Set.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE)) {
//            FileTreeWalker.Event ev = walker.walk(rootFile.toPath());
//            do {
//                FileVisitResult result;
//                switch (ev.type()) {
//                    case ENTRY :
//                        IOException ioe = ev.ioeException();
//                        if (ioe == null) {
//                            assert ev.attributes() != null;
//                            result = visitor.visitFile(ev.file(), ev.attributes());
//                        } else {
//                            result = visitor.visitFileFailed(ev.file(), ioe);
//                        }
//                        break;
//
//                    case START_DIRECTORY :
//                        result = visitor.preVisitDirectory(ev.file(), ev.attributes());
//
//                        // if SKIP_SIBLINGS and SKIP_SUBTREE is returned then
//                        // there shouldn't be any more events for the current
//                        // directory.
//                        if (result == FileVisitResult.SKIP_SUBTREE ||
//                                result == FileVisitResult.SKIP_SIBLINGS)
//                            walker.pop();
//                        break;
//
//                    case END_DIRECTORY :
//                        result = visitor.postVisitDirectory(ev.file(), ev.ioeException());
//
//                        // SKIP_SIBLINGS is a no-op for postVisitDirectory
//                        if (result == FileVisitResult.SKIP_SIBLINGS)
//                            result = FileVisitResult.CONTINUE;
//                        break;
//
//                    default :
//                        throw new AssertionError("Should not get here");
//                }
//
//                if (Objects.requireNonNull(result) != FileVisitResult.CONTINUE) {
//                    if (result == FileVisitResult.TERMINATE) {
//                        break;
//                    } else if (result == FileVisitResult.SKIP_SIBLINGS) {
//                        walker.skipRemainingSiblings();
//                    }
//                }
//                ev = walker.next();
//            } while (ev != null);
//        }
//
//        return start;

    }

    private String[] splitPath(String path) {
        splitRegex = Matcher.quoteReplacement(System.getProperty("file.separator"));
        return path.split(splitRegex);
    }

    private void clean(Path path) {

    }

    private void store(Path dir, IOException exc) {
        System.out.println(exc.getMessage());
    }

    private void store(Path file, BasicFileAttributes attrs) {
        Scanner.FileCounter.incrementAndGet();
        String pathString = file.toString();
        Scanner.FileString.set(pathString);
        String[] path = splitPath(pathString);
        Node current = root;
        for (String entry : path) {
            Node child = current.getChildByName(entry);
            if (child != null) {
                current = child;
                continue;
            }
            child = new Node(current, entry);
            current.addChild(child);
        }
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

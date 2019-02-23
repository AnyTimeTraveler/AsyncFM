package org.simonscode.asyncfm.common;

import org.simonscode.asyncfm.scanner.Scanner;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient Node parent;
    private final boolean isDirectory;
    private final String name;
    private final long size;
    private List<Node> children = new ArrayList<>();
    private final long hash;

    public Node(Node parent, File file) {
        this.parent = parent;
        isDirectory = file.isDirectory();
        Scanner.FileCounter.incrementAndGet();
        Scanner.FileString.set(file.getAbsolutePath());
        size = file.length();
        name = file.getName();
        if (isDirectory) {
            hash = 0L;
        } else {
            hash = Hasher.hash(file);
        }
    }

    public void scanForChildren(File file) {
        if (!isDirectory) {
            return;
        }
        File[] files = file.listFiles();
        if (files == null) {
            System.err.println("Could not list files for path: " + file.getAbsolutePath());
            return;
        }
        for (File child : files) {
            if (Files.isSymbolicLink(Paths.get(child.toURI()))) {
                continue;
            }
            Node e = new Node(this, child);
            children.add(e);
            e.scanForChildren(child);
        }
    }

    public void restoreParents(Node parent) {
        this.parent = parent;
        if (children == null)
            return;
        for (Node child : children) {
            child.restoreParents(this);
        }
    }

    public long countChildren() {
        if (children == null || children.size() == 0) {
            return 1L;
        }
        long amount = 0L;
        for (Node child : children) {
            amount += child.countChildren();
        }
        return amount;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public long getAbsoluteSize() {
        if (children == null || children.size() == 0) {
            return getSize();
        }
        long amount = 0L;
        for (Node child : children) {
            amount += child.getAbsoluteSize();
        }
        return amount;
    }

    public List<Node> getChildren() {
        return children;
    }

    public long getHash() {
        return hash;
    }

    public Node getParent() {
        return parent;
    }

    public String getPath() {
        return (parent != null ? parent.getPath() : "") + "/" + name;
    }
}

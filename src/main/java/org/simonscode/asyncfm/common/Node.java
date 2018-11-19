package org.simonscode.asyncfm.common;

import org.simonscode.asyncfm.scanner.Scanner;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean isDirectory;
    private final String name;
    private final long size;
    private List<Node> children = new ArrayList<>();

    public Node(File file) {
        isDirectory = file.isDirectory();
        Scanner.FileCounter.incrementAndGet();
        Scanner.FileString.set(file.getAbsolutePath());
        size = file.length();
        name = file.getName();
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
            Node e = new Node(child);
            children.add(e);
            e.scanForChildren(child);
        }
    }

    public long countChildren(){
        if (children == null || children.size() == 0){
            return 1L;
        }
        long amount = 0L;
        for (Node child: children){
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

    public List<Node> getChildren() {
        return children;
    }
}

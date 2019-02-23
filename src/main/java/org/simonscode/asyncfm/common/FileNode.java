package org.simonscode.asyncfm.common;

public class FileNode extends Node {
    private final long size;
    private final long hash;

    public FileNode(long id, Node parent, String name, long size, long hash) {
        super(id, parent, name);
        this.size = size;
        this.hash = hash;
    }

    @Override
    public long getAbsoluteSize() {
        return getSize();
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public long getSize() {
        return size;
    }

    public long getHash() {
        return hash;
    }
}

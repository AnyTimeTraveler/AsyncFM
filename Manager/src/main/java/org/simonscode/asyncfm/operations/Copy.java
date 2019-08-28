package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

public class Copy extends Transaction {
    private final Node src;
    private final Node srcCopy;
    private final Node dst;

    public Copy(final Node src, final Node dst) {
        this.src = src;
        this.srcCopy = new Node(src);
        this.dst = dst;
    }

    @Override
    public void execute() {
        dst.addChild(srcCopy);
    }

    @Override
    public void undo() {
        dst.removeChild(srcCopy);
    }

    @Override
    public String toString() {
        return String.format("Copy %s to %s", src.getPath(), dst.getPath());
    }
}

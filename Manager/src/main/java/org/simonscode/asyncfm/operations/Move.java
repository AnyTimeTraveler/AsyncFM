package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

public class Move extends Transaction {
    private final Node src;
    private final Node parent;
    private final Node dst;

    public Move(final Node src, final Node dst) {
        this.src = src;
        parent = (Node) src.getParent();
        this.dst = dst;
    }

    @Override
    public void execute() {
        parent.removeChild(src);
        dst.addChild(src);
    }

    @Override
    public void undo() {
        dst.removeChild(src);
        parent.addChild(src);
    }

    @Override
    public String toString() {
        return String.format("Move %s to %s", src.getPath(), dst.getPath());
    }
}

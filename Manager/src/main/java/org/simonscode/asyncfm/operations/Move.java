package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

public class Move extends Transaction {
    private final Node src;
    private final Node oldParent;
    private final Node dst;

    public Move(final Node src, final Node dst) {
        this.src = src;
        oldParent = src.getParent();
        this.dst = dst;
    }

    @Override
    public void execute() {
        oldParent.removeChild(src);
        dst.addChild(src);
    }

    @Override
    public void undo() {
        dst.removeChild(src);
        oldParent.addChild(src);
    }

    @Override
    public String toString() {
        return String.format("Move %s to %s", src.getPath(), dst.getPath());
    }

    @Override
    public String getKind() {
        return "Move";
    }
}

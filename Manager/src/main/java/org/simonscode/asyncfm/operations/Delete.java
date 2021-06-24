package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

public class Delete extends Transaction {
    private final Node node;

    public Delete(final Node node) {
        this.node = node;
    }

    @Override
    public void execute() {
        node.getParent().removeChild(node);
    }

    @Override
    public void undo() {
        node.getParent().addChild(node);
    }

    @Override
    public String toString() {
        return String.format("Delete %s", node.getPath());
    }

    @Override
    public String getKind() {
        return "Delete";
    }
}

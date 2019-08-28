package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

public class Rename extends Transaction {
    private final Node node;
    private final String oldName;
    private final String name;

    public Rename(final Node node, final String name) {
        this.node = node;
        this.oldName = node.getName();
        this.name = name;
    }

    @Override
    public void execute() {
        node.setName(name);
    }

    @Override
    public void undo() {
        node.setName(oldName);
    }

    @Override
    public String toString() {
        return String.format("Rename %s to %s", node.getPath(), name);
    }
}

package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;

public class CreateFolder extends Transaction {
    private final Node parent;
    private final Node child;

    public CreateFolder(final Node parent, final String name) {
        this.parent = parent;
        child = new Node(parent, name, true);
    }

    @Override
    public void execute() {
        parent.addChild(child);
    }

    @Override
    public void undo() {
        parent.removeChild(child);
    }

    @Override
    public String toString() {
        return String.format("Create folder named %s in %s", child.getName(), parent.getPath());
    }

    @Override
    public String getKind() {
        return "Create folder";
    }
}

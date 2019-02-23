package org.simonscode.asyncfm.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    private final transient long id;
    private final Node parent;
    private final String name;
    private final List<Node> children;

    public Node(long id, Node parent, String name) {
        this.id = id;
        this.parent = parent;
        this.name = name;
        this.children = new ArrayList<>();
    }

    public long countChildren() {
        if (children.isEmpty()) {
            return 0L;
        }
        long amount = 0L;
        for (Node child : children) {
            amount += child.countChildren() + 1L;
        }
        return amount;
    }

    public boolean isDirectory() {
        return true;
    }

    public String getName() {
        return name;
    }

    public long getAbsoluteSize() {
        if (children.isEmpty()) {
            return getSize();
        }
        long amount = 0L;
        for (Node child : children) {
            amount += child.getAbsoluteSize();
        }
        return amount;
    }

    public long getSize() {
        return 0L;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Node getParent() {
        return parent;
    }

    public String getPath() {
        return (parent != null ? parent.getPath() : "") + "/" + name;
    }

    public long getId() {
        return id;
    }

    public void addChild(Node node) {
        children.add(node);
    }
}

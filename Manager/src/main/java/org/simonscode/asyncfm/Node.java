package org.simonscode.asyncfm;

import javax.swing.tree.TreeNode;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Node implements TreeNode {

    private Node parent;
    private String name;
    private boolean isDirectory;
    private final List<Node> children;
    private long size;
    private long hash;

    public Node(Node parent, String name) {
        this.isDirectory = true;
        this.parent = parent;
        this.name = name;
        this.children = new ArrayList<>();
    }

    public Node(Node parent, String name, long size, long hash) {
        this(parent, name);
        this.isDirectory = false;
        this.size = size;
        this.hash = hash;
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

    public long getAbsoluteSize() {
        if (children.isEmpty()) {
            return size;
        }
        long amount = 0L;
        for (Node child : children) {
            amount += child.getAbsoluteSize();
        }
        return amount;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public List<Node> getChildren() {
        return children;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    public String getPath() {
        return (parent != null ? parent.getPath() : "") + "/" + name;
    }

    public void addChild(Node node) {
        children.add(node);
    }

    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }


    @Override
    public int getIndex(TreeNode node) {
        if (!(node instanceof Node)) {
            throw new InvalidParameterException("child is not instance of Node");
        }
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return isDirectory;
    }

    @Override
    public boolean isLeaf() {
        return !isDirectory;
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return Collections.enumeration(children);
    }

    public Node getChildByName(String entry) {
        for (Node node : children)
            if (node.name.equals(entry))
                return node;
        return null;
    }
}

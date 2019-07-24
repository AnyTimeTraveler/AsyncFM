package org.simonscode.asyncfm;

import javax.swing.tree.TreeNode;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Node implements TreeNode {
    long id;            //  u64
    long parent_id;        //  u64
    String name;        //  &'t[u8]
    byte flags;            //  u8
    int mode;            //  u32
    int uid;            //  u32
    int gid;            //  u32
    long size;            //  u64
    long created;        //  i64
    long modified;        //  i64
    long accessed;        //  i64
    String link_dest;    //  Option<Vec<u8>>
    int hash;            //  Option<u32>

    private Node parent;
    private final List<Node> children;

    public Node(DataInputStream dis) throws IOException {
        children = new ArrayList<>();

        id = dis.readLong();
        parent_id = dis.readLong();
        name = readString(dis);
        flags = dis.readByte();
        mode = dis.readByte();
        uid = dis.readInt();
        gid = dis.readInt();
        size = dis.readLong();
        created = dis.readLong();
        modified = dis.readLong();
        accessed = dis.readLong();
        link_dest = readString(dis);
        hash = dis.readInt();

    }

    private String readString(DataInputStream dis) throws IOException {
        byte[] data = new byte[1024];
        byte read = dis.readByte();
        int i;
        for (i = 0; read != 0; i++) {
            if (i == data.length) {
                byte[] replacement = new byte[data.length + 1024];
                System.arraycopy(data, 0, replacement, 0, data.length);
                data = replacement;
            }
            data[i] = read;
            read = dis.readByte();
        }
        return new String(data, 0, i, Charset.forName("UTF-8"));
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
//        return isDirectory;
        return false;
    }


    public void setDirectory(boolean directory) {
//        isDirectory = directory;
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
//        return isDirectory;
        return false;
    }

    @Override
    public boolean isLeaf() {
//        return !isDirectory;
        return false;
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

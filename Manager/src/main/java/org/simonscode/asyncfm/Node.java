package org.simonscode.asyncfm;

import org.simonscode.asyncfm.gui.FileSize;

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
        mode = dis.readInt();
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

    public FileSize getAbsoluteSizeString() {
        return new FileSize(getAbsoluteSize());
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

    private void setParent(Node targetLocation) {
        this.parent = targetLocation;
    }

    public void move(Node targetLocation) {
        parent.removeChild(this);
        targetLocation.addChild(this);
    }

    public void addChild(Node child) {
        child.setParent(this);
        children.add(child);
    }

    private void removeChild(Node child) {
        children.remove(child);
    }

    public String getName() {
        return name;
    }

    public void rename(String name) {
        this.name = name;
    }

    public boolean isDirectory() {
        return (flags & 0b00000001) == 0b00000001;
    }

    public boolean isSymlink() {
        return (flags & 0b00000100) == 0b00000100;
    }

    public boolean isFile() {
        return (flags & 0b00000010) == 0b00000010;
    }

    public List<Node> getChildren() {
        return children;
    }

    public long getSize() {
        return size;
    }

    public FileSize getSizeString() {
        return new FileSize(getAbsoluteSize());
    }

    public boolean hasHash() {
        return (flags & 0b00001000) == 0b00001000;
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
        return isDirectory();
    }

    @Override
    public boolean isLeaf() {
        return isFile();
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

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", parent_id=" + parent_id +
                ", name='" + name + '\'' +
                ", flags=" + flags +
                ", mode=" + mode +
                ", uid=" + uid +
                ", gid=" + gid +
                ", size=" + size +
                ", created=" + created +
                ", modified=" + modified +
                ", accessed=" + accessed +
                ", link_dest='" + link_dest + '\'' +
                ", hash=" + hash +
                ", parent=" + parent +
                ", children=" + children.size() +
                '}';
    }
}

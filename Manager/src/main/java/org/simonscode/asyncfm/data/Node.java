package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.gui.FileSize;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.*;

import static org.simonscode.asyncfm.data.StructUtils.readString;

public class Node implements TreeNode {

    private static long entriesCount;

    long id;            //  u64
    long parent_id;     //  u64
    String name;        //  &'t[u8]
    /**
     * Flags: [1234 5678]
     * <p>
     * 1: reserved
     * 2: reserved
     * 3: reserved
     * 4: reserved
     * 5: true if hash exists
     * 6: true if symlink
     * 7: true if file
     * 8: true if directory
     */
    private byte flags;         //  u8
    private int mode;           //  u32
    private int uid;            //  u32
    private int gid;            //  u32
    private long size;          //  u64
    private long created;       //  i64
    private long modified;      //  i64
    private long accessed;      //  i64
    private String link_dest;   //  &'t[u8]
    private int hash;           //  u32

    private Node parent;
    private final List<Node> children;
    private boolean marked;

    public Node(DataInputStream dis) throws IOException {
        children = new ArrayList<>();

        parent = null;
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
        marked = false;
    }

    /**
     * Copy constructor used for copying this node and its children.
     * It is intended to be used for making a perfect copy of this node, except for setting the parent.
     * This allows this node to become a new root.
     * <p>
     * Important: This also generated new ID's for these files, so they can be differentiated from the originals.
     *
     * @param node The Node to be copied
     */
    public Node(Node node) {
        children = new ArrayList<>();

        this.parent = null;
        this.id = ++entriesCount;
        this.parent_id = 0L;
        this.name = node.name;
        this.flags = node.flags;
        this.mode = node.mode;
        this.uid = node.uid;
        this.gid = node.gid;
        this.size = node.size;
        this.created = node.created;
        this.modified = node.modified;
        this.accessed = node.accessed;
        this.link_dest = node.link_dest;
        this.hash = node.hash;
        this.marked = node.marked;

        for (Node child : node.children) {
            addChild(new Node(child));
        }
    }

    public Node(Node parent, String name) {
        children = new ArrayList<>();

        this.parent = null;
        this.id = ++entriesCount;
        this.parent_id = 0L;
        this.name = name;
        // TODO: Replace with configurable values
        this.flags = parent.flags;
        this.mode = parent.mode;
        this.uid = parent.uid;
        this.gid = parent.gid;
        this.size = 0L;
        // TODO: Replace with current time
        this.created = Instant.now().getEpochSecond();
        this.modified = Instant.now().getEpochSecond();
        this.accessed = Instant.now().getEpochSecond();
        this.link_dest = null;
        this.hash = 0;
        this.marked = false;
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

    public void addChild(Node child) {
        children.add(child);
        child.setParent(this);
        child.setParentId(id);
    }

    public void removeChild(Node child) {
        children.remove(child);
        child.setParent(null);
        child.setParentId(0L);
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

    public FileSize getSizeString() {
        return new FileSize(getAbsoluteSize());
    }

    public boolean hasHash() {
        return (flags & 0b00001000) == 0b00001000;
    }

    public String getPath() {
        return (parent != null ? parent.getPath() : "") + "/" + name;
    }

    public Node getChildByName(String entry) {
        for (Node node : children)
            if (node.name.equals(entry))
                return node;
        return null;
    }

    public TreePath getTreePath() {
        List<TreeNode> nodes = new LinkedList<>();
        TreeNode treeNode = this;
        nodes.add(treeNode);
        treeNode = treeNode.getParent();
        while (treeNode != null) {
            nodes.add(0, treeNode);
            treeNode = treeNode.getParent();
        }

        return nodes.isEmpty() ? null : new TreePath(nodes.toArray());

    }

    //=================================//
    //    TreeNode required Methods    //
    //=================================//

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
        return isFile() || children.isEmpty();
    }

    @Override
    public Enumeration<? extends TreeNode> children() {
        return Collections.enumeration(children);
    }

    //=================================//
    //  Generated Getters and Setters  //
    //=================================//

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    private long getParentId() {
        return parent_id;
    }

    private void setParentId(long parent_id) {
        this.parent_id = parent_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public long getAccessed() {
        return accessed;
    }

    public void setAccessed(long accessed) {
        this.accessed = accessed;
    }

    public String getLinkDest() {
        return link_dest;
    }

    public void setLinkDest(String link_dest) {
        this.link_dest = link_dest;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public static void setEntriesCount(long entriesCount) {
        Node.entriesCount = entriesCount;
    }

    //=======================//
    //  Common Java Methods  //
    //=======================//

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

package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.gui.FileSize;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.io.DataInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.util.*;

import static org.simonscode.asyncfm.data.NodeWalker.readString;

public class Node implements TreeNode {

    private static long entriesCount;

    long id;                        //  u64
    long parentId;                  //  u64
    String name;                    //  &[u8]
    // private byte flags;          //  u8
    private boolean isSymlink;
    private boolean isFile;
    private boolean isDirectory;
    private int mode;               //  u32
    private int uid;                //  u32
    private int gid;                //  u32
    private long size;              //  u64
    private long created;           //  i64
    private long modified;          //  i64
    private long accessed;          //  i64
    private String linkDestination; //  &[u8]
    private int hash;               //  u32

    private Node parent;
    private final List<Node> children;
    private boolean marked;

    public Node(DataInputStream dis) throws IOException {
        children = new ArrayList<>();

        parent = null;
        id = dis.readLong();
        parentId = dis.readLong();
        name = readString(dis);
        parseFlags(dis.readByte());
        mode = dis.readInt();
        uid = dis.readInt();
        gid = dis.readInt();
        size = dis.readLong();
        created = dis.readLong();
        modified = dis.readLong();
        accessed = dis.readLong();
        linkDestination = readString(dis);
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
        this.parentId = 0L;
        this.name = node.name;
        this.isSymlink = node.isSymlink;
        this.isFile = node.isFile;
        this.isDirectory = node.isDirectory;
        this.mode = node.mode;
        this.uid = node.uid;
        this.gid = node.gid;
        this.size = node.size;
        this.created = node.created;
        this.modified = node.modified;
        this.accessed = node.accessed;
        this.linkDestination = node.linkDestination;
        this.hash = node.hash;
        this.marked = node.marked;

        for (Node child : node.children) {
            addChild(new Node(child));
        }
    }

    public Node(Node parent, String name, boolean isDirectory) {
        children = new ArrayList<>();

        this.parent = null;
        this.id = ++entriesCount;
        this.parentId = 0L;
        this.name = name;
        this.isSymlink = false;
        this.isFile = !isDirectory;
        this.isDirectory = isDirectory;
        this.mode = parent.mode;
        this.uid = parent.uid;
        this.gid = parent.gid;
        this.size = 0L;
        this.created = Instant.now().getEpochSecond();
        this.modified = Instant.now().getEpochSecond();
        this.accessed = Instant.now().getEpochSecond();
        this.linkDestination = null;
        this.hash = 0;
        this.marked = false;
    }

    public Node(Node parent, String name, String linkDestination) {
        this(parent, name, false);
        this.linkDestination = linkDestination;
        this.isSymlink = true;
        this.isFile = false;
        this.isDirectory = false;
    }

    public static long getEntriesCount() {
        return entriesCount;
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

    public static void setEntriesCount(long entriesCount) {
        Node.entriesCount = entriesCount;
    }

    private void parseFlags(byte flags) {
        isSymlink = (flags & 0b00000001) != 0;
        isFile = (flags & 0b00000010) != 0;
        isDirectory = (flags & 0b00000100) != 0;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public List<Node> getChildren() {
        return children;
    }

    public FileSize getSizeString() {
        return new FileSize(getAbsoluteSize());
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
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

    public boolean isSymlink() {
        return isSymlink;
    }

    public void setSymlink(boolean symlink) {
        isSymlink = symlink;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasHash() {
        return hash != 0;
    }

    public long getParentId() {
        return parentId;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
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

    public String getLinkDestination() {
        return linkDestination;
    }

    public void setLinkDestination(String linkDestination) {
        this.linkDestination = linkDestination;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", parent_id=" + parentId +
                ", name='" + name + '\'' +
                ", isSymlink=" + isSymlink +
                ", isFile=" + isFile +
                ", isDirectory=" + isDirectory +
                ", mode=" + mode +
                ", uid=" + uid +
                ", gid=" + gid +
                ", size=" + size +
                ", created=" + created +
                ", modified=" + modified +
                ", accessed=" + accessed +
                ", linkDestination='" + linkDestination + '\'' +
                ", hash=" + hash +
                ", parent=" + (parent != null ? parent.getPath() : "null") +
                ", children=" + children.size() +
                ", marked=" + marked +
                '}';
    }
}

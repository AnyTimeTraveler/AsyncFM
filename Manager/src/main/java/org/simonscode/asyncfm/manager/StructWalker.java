package org.simonscode.asyncfm.manager;

import org.simonscode.asyncfm.common.Node;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

public class StructWalker {

    private final DataInputStream dis;
    private final AtomicLong fileCounter;

    public StructWalker(InputStream is) {
        this.dis = new DataInputStream(is);
        this.fileCounter = new AtomicLong();
    }


    public Node readTree() throws IOException {
        Node root = readNode(new Node(null, "/"));
        Node current = root;
        while (dis.available() > 0) {
            current = readNode(current);
        }

        return root;
    }

    private Node readNode(Node parent) throws IOException {
        Node node;

        long id = fileCounter.incrementAndGet();
        long parentId = dis.readLong();
        byte[] nameBytes = new byte[dis.readInt()];

        // we assume this works, since it'll crash below if it doesn't
        //noinspection ResultOfMethodCallIgnored
        dis.read(nameBytes);
        String name = new String(nameBytes, Charset.forName("UTF-8"));
        boolean isFile = dis.readBoolean();
//        while (parentId != parent.getId()) {
//            parent = (Node) parent.getParent();
//        }
        if (isFile) {
            long size = dis.readLong();
            long hash = dis.readLong();
            node = new Node(parent, name, size, hash);
        } else {
            node = new Node(parent, name);
        }

        parent.addChild(node);

        return node;
    }
}

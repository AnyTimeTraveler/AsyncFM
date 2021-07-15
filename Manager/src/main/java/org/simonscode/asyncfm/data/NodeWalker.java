package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.data.headers.Header;
import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.gui.LoadingDialog;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NodeWalker {

    private final DataInputStream dis;
    private final ArrayList<Node> nodes;
    private final Header header;
    private final LoadingDialog loadingDialog;

    public NodeWalker(InputStream is, boolean showDialog, AsyncFMFrame parent) throws IOException {
        dis = new DataInputStream(is);
        nodes = new ArrayList<>();
        header = Header.fromBytes(dis);
        Node.setEntriesCount(header.getEntries());
        loadingDialog = new LoadingDialog(header.getEntries());

        if (showDialog) {
            SwingUtilities.invokeLater(() -> loadingDialog.show(parent));
        }
    }

    public static void showMemory() {
        int mb = 1024 * 1024;

        // get Runtime instance
        Runtime instance = Runtime.getRuntime();
        System.out.printf(
                """
                           Used  |  Total  |   Max
                         % 4d MB | % 4d MB | % 4d MB
                        """,
                (instance.totalMemory() - instance.freeMemory()) / mb,
                instance.totalMemory() / mb,
                instance.maxMemory() / mb
        );
    }

    public Node readTree() throws IOException {
        System.out.println();
        System.out.println();

        showMemory();

        // Read all nodes
        System.out.print("\nReading data...");
        for (long i = 0; dis.available() > 0; i++) {
            var node = new Node(dis);
            nodes.add(node);
            loadingDialog.setProgress(0, i);
        }
        System.out.println("Done!");

        showMemory();

        // organize in tree structure
        System.out.print("\nGetting nodes by their ids...");
        Map<Long, Node> idToNodeMap = new TreeMap<>();
        Iterator<Node> nodeIterator = nodes.iterator();
        for (long i = 0; nodeIterator.hasNext(); i++) {
            Node node = nodeIterator.next();
            idToNodeMap.put(node.id, node);
            loadingDialog.setProgress(1, i);
        }
        System.out.println("Done!");

        showMemory();

        System.out.print("\nGetting nodes by their parents...");
        Map<Long, LinkedList<Node>> parents = new TreeMap<>();
        nodeIterator = nodes.iterator();
        for (long i = 0; nodeIterator.hasNext(); i++) {
            Node node = nodeIterator.next();
            if (!parents.containsKey(node.parentId)) {
                parents.put(node.parentId, new LinkedList<>());
            }
            parents.get(node.parentId).add(node);
            loadingDialog.setProgress(2, i);
        }
        System.out.println("Done!");

        showMemory();

        System.out.print("\nOrganizing tree structure...");
        Node root = idToNodeMap.get(0L);
        long i = 0;
        for (LinkedList<Node> siblings : parents.values()) {
            Node parent = idToNodeMap.get(siblings.getFirst().parentId);
            if (parent == null) {
                System.out.println("NULL : " + siblings.getFirst().name);
                continue;
            }
            for (Node child : siblings) {
                if (child.id != parent.id)
                    parent.addChild(child);
                i++;
                loadingDialog.setProgress(3, i);
            }
        }
        System.out.println("Done!");

        showMemory();

        loadingDialog.close();
        // return root node
        return root;
    }

    public static String readString(DataInputStream dis) throws IOException {
        int length = dis.readInt();

        if (length <= 0) {
            return null;
        }
        byte[] data = new byte[length];

        int bytesRead = dis.read(data, 0, data.length);
        if (bytesRead != data.length) {
            throw new IOException("Failed to read");
        }
        return new String(data, 0, data.length, StandardCharsets.UTF_8);
    }

    public void writeData(Node rootNode, FileOutputStream fos) {

    }
}

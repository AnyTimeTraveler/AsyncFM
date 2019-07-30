package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.data.headers.Header;
import org.simonscode.asyncfm.gui.LoadingDialog;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.*;

public class StructUtils {

    private final DataInputStream dis;
    private final ArrayList<Node> nodes;
    private final Header header;
    private final LoadingDialog loadingDialog;

    public StructUtils(InputStream is, boolean showDialog) throws IOException {
        dis = new DataInputStream(is);
        nodes = new ArrayList<>();
        header = Header.fromBytes(dis);
        loadingDialog = new LoadingDialog(header.getEntries());

        if (showDialog) {
            SwingUtilities.invokeLater(loadingDialog::show);
        }
    }


    public Node readTree() throws IOException {
        System.out.println();
        System.out.println();
        showMemory();
        // Read all nodes
        System.out.print("Reading data...");
        for (long i = 0; dis.available() > 0; i++) {
            Node node = new Node(dis);
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
            if (!parents.containsKey(node.parent_id)) {
                parents.put(node.parent_id, new LinkedList<>());
            }
            parents.get(node.parent_id).add(node);
            loadingDialog.setProgress(2, i);
        }
        System.out.println("Done!");

        showMemory();
        System.out.print("\nOrganizing tree structure...");
        Node root = idToNodeMap.get(0L);
        long i = 0;
        for (LinkedList<Node> siblings : parents.values()) {
            Node parent = idToNodeMap.get(siblings.getFirst().parent_id);
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

        loadingDialog.close();
        // return root node
        return root;
    }

    public static void showMemory() {
        int mb = 1024 * 1024;

        // get Runtime instance
        Runtime instance = Runtime.getRuntime();
        String pattern = "###,###.###";
        DecimalFormat decimalFormat = new DecimalFormat(pattern);

        System.out.printf(
                "Used | Total | Max \n %s MB | %s MB | %s MB \n",
                decimalFormat.format((instance.totalMemory() - instance.freeMemory()) / mb),
                decimalFormat.format(instance.totalMemory() / mb),
                decimalFormat.format(instance.maxMemory() / mb)
        );
    }

    public static String readString(DataInputStream dis) throws IOException {
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
}

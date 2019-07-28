package org.simonscode.asyncfm;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class StructWalker {

    private final DataInputStream dis;
    private final ArrayList<Node> nodes;

    public StructWalker(InputStream is) {
        dis = new DataInputStream(is);
        nodes = new ArrayList<>();
    }


    public Node readTree() throws IOException {
        System.out.println();
        System.out.println();
        showMemory();
        // Read all nodes
        System.out.print("Reading data...");
        while (dis.available() > 0) {
            Node node = new Node(dis);
            nodes.add(node);
        }
        System.out.println("Done!");

        showMemory();

        // organize in tree structure
        System.out.print("\nGetting nodes by their ids...");
        Map<Long, Node> idToNodeMap = new TreeMap<>();
        for (Node node : nodes) {
            idToNodeMap.put(node.id, node);
        }
        System.out.println("Done!");

        showMemory();

        System.out.print("\nGetting nodes by their parents...");
        Map<Long, LinkedList<Node>> parents = new TreeMap<>();
        for (Node node : nodes) {
            if (!parents.containsKey(node.parent_id)) {
                parents.put(node.parent_id, new LinkedList<>());
            }
            parents.get(node.parent_id).add(node);
        }
        System.out.println("Done!");

        showMemory();
        System.out.print("\nOrganizing tree structure...");
        Node root = idToNodeMap.get(0L);
        for (LinkedList<Node> siblings : parents.values()) {
            Node parent = idToNodeMap.get(siblings.getFirst().parent_id);
            if (parent == null){
                System.out.println("NULL : " + siblings.getFirst().name);
                continue;
            }
            for (Node child : siblings) {
                if (child.id != parent.id)
                parent.addChild(child);
            }
        }

        // return root node
        return root;
    }

    private static void showMemory() {
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
}

package org.simonscode.asyncfm.manager;

import org.simonscode.asyncfm.common.Node;
import org.simonscode.asyncfm.common.RootNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Comparator;
import java.util.List;

public class Manager {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.print("Reading file...");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("result.struct"));
        RootNode root = (RootNode) ois.readObject();
        System.out.println("Done!");

        System.out.printf("%nChildren: %s%n", root.countChildren());
        List<Node> nodes = root.getChildren();
        nodes.sort(Comparator.comparing(Node::getName));
        for (Node n : nodes) {
            System.out.printf("%-30s %8s%n", n.getName(), humanReadableByteCount(n.getSize(), true));
        }
    }


    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

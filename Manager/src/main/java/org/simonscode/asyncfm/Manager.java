package org.simonscode.asyncfm;

import org.simonscode.asyncfm.gui.FileManager;

import java.io.FileInputStream;
import java.io.IOException;

public class Manager {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Arguments: <input file>");
            return;
        }

        System.out.print("Reading file...");
        try (FileInputStream fis = new FileInputStream(args[0])) {
            StructWalker walker = new StructWalker(fis);
            Node root = walker.readTree();
            System.out.println("Done!");

            System.out.printf("%n%s Files loaded.", root.countChildren());
//            List<Node> nodes = root.getChildren();
//            nodes.sort(Comparator.comparing(Node::getName));
//            for (Node n : nodes) {
//                System.out.printf("%-30s %8s%n", n.getName(), humanReadableByteCount(n.getAbsoluteSize(), true));
//            }
//            FileManager fileManager = new FileManager(root);
//            fileManager.createAndShowGui();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}

package org.simonscode.asyncfm;

import org.simonscode.asyncfm.gui.FileManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

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

            System.out.printf("%n%s Files loaded.", root.countChildren() + 1);
//            List<Node> nodes = root.getChildren();
//            nodes.sort(Comparator.comparing(Node::getName));
//            for (Node n : nodes) {
//                System.out.printf("%-30s %8s%n", n.getName(), humanReadableByteCount(n.getAbsoluteSize(), true));
//            }
            FileManager fileManager = new FileManager(root);
            fileManager.createAndShowGui();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

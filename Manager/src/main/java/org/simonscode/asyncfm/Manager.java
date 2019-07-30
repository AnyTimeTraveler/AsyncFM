package org.simonscode.asyncfm;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.StructUtils;
import org.simonscode.asyncfm.gui.FileManager;

import java.io.FileInputStream;
import java.io.IOException;

public class Manager {
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Arguments: <input file>");
            return;
        }

        try (FileInputStream fis = new FileInputStream(args[0])) {
            StructUtils walker = new StructUtils(fis, true);
            Node root = walker.readTree();

            System.out.printf("%n%s Files loaded.", root.countChildren() + 1);
            FileManager fileManager = new FileManager(root);
            fileManager.createAndShowGui();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

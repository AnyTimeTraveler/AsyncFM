package org.simonscode.asyncfm;

import org.simonscode.asyncfm.data.StructUtils;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.FileManager;

import java.io.FileInputStream;
import java.io.IOException;

public class Manager {
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Arguments: <input file>");
            return;
        }

        var fis = new FileInputStream(args[0]);
        var walker = new StructUtils(fis, true);

        var fileManager = new FileManager(walker.readTree());
        TransactionStore.setFileManager(fileManager);
        fileManager.createAndShowGui();
    }
}

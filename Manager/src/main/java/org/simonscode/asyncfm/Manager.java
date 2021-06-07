package org.simonscode.asyncfm;

import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;

public class Manager {
    public static void main(String[] args) {
        try {
            // Significantly improves the look of the output in
            // terms of the node names returned by FileSystemView!
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        AsyncFMFrame frame;
        if (args.length == 1) {
            frame = new AsyncFMFrame(args[0]);
        } else {
            frame = new AsyncFMFrame();
        }
        frame.setVisible(true);
    }
}

package org.simonscode.asyncfm.gui.transactions;

import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TransactionsPanel extends JPanel {

    public TransactionsPanel(AsyncFMFrame parent) {
        setLayout(new BorderLayout(3, 3));
        setBorder(new EmptyBorder(5, 5, 5, 5));
    }
}

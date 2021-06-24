package org.simonscode.asyncfm.gui.transactions;

import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.operations.Transaction;

import javax.swing.*;
import java.awt.*;

public class TransactionPanel extends JPanel {
    public TransactionPanel(AsyncFMFrame parent, Transaction transaction) {
        setLayout(new BorderLayout(3, 3));
        setBorder(BorderFactory.createTitledBorder(transaction.getKind()));


    }
}

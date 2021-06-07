package org.simonscode.asyncfm.gui.transactions;

import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TransactionPanel extends JPanel {

    public TransactionPanel(AsyncFMFrame parent) {
        setLayout(new BorderLayout(3, 3));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        JTable transactionTable = new JTable(new TransactionTableModel());
        transactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionTable.setAutoCreateRowSorter(true);
        transactionTable.setShowVerticalLines(false);
        add(transactionTable, BorderLayout.CENTER);
    }

    public void onFileTreeUpdated() {

    }
}

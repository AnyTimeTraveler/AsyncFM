package org.simonscode.asyncfm.gui;

import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.operations.Transaction;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * A TableModel to hold File[].
 */
class TransactionTableModel extends AbstractTableModel {

    private String[] columns = {
            "Action",
            ""
    };

    public Object getValueAt(int row, int column) {
        Transaction transaction = TransactionStore.getTransactions().get(row);

        switch (column) {
            case 0:
                return transaction.toString();
            case 1:
                new JButton("Undo");
            default:
                System.err.println("Logic Error" + column);
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        if (column == 1) {
            return JButton.class;
        }
        return String.class;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return TransactionStore.getTransactions().size();
    }
}
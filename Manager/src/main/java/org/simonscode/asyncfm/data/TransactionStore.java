package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.operations.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionStore {

    private static final List<Transaction> transactions = new ArrayList<>();
    private static AsyncFMFrame frame;


    public static void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.execute();
        frame.onFileTreeUpdated();
    }

    public static void setFrame(AsyncFMFrame frame) {
        TransactionStore.frame = frame;
    }

    public static List<Transaction> getTransactions() {
        return transactions;
    }
}

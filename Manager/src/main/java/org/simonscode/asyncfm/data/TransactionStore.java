package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.gui.FileManager;
import org.simonscode.asyncfm.operations.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionStore {

    private static final List<Transaction> transactions = new ArrayList<>();
    private static FileManager fileManager;


    public static void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.execute();
        fileManager.update();
    }

    public static void setFileManager(FileManager fileManager) {
        TransactionStore.fileManager = fileManager;
    }
}

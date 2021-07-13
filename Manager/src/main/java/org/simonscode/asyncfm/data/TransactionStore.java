package org.simonscode.asyncfm.data;

import org.simonscode.asyncfm.gui.filebrowser.FileBrowserPanel;
import org.simonscode.asyncfm.operations.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionStore {

    private static final List<Transaction> transactions = new ArrayList<>();
    private static FileBrowserPanel fileBrowser;

    public static void executeAndLogTransaction(Transaction transaction) {
        transactions.add(transaction);
        transaction.execute();
        fileBrowser.onFileTreeUpdated();
    }

    public static void undoAndRemove(Transaction transaction) {
        transaction.undo();
        transactions.remove(transaction);
        fileBrowser.onFileTreeUpdated();
    }

    public static void setFileBrowser(FileBrowserPanel fileBrowser) {
        TransactionStore.fileBrowser = fileBrowser;
    }

    public static List<Transaction> getTransactions() {
        return transactions;
    }
}

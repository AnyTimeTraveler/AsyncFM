package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.FileManager;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TransactionCreator {

    private static Node firstSelection;
    private static ArrayList<ResetAction> resetActions = new ArrayList<>();

    public static void handleClick(Class<? extends Transaction> action, JButton button, Node node, FileManager fileManager) {
        if (action == Copy.class || action == Move.class) {
            if (firstSelection == null) {
                resetActions.add(new ResetAction(button));
                button.setText("Set destination folder");
                firstSelection = node;
            } else {
                try {
                    TransactionStore.addTransaction(action.getConstructor(Node.class, Node.class).newInstance(firstSelection, node));
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    fileManager.showThrowable(e);
                }
                resetState();
            }
        } else if (action == Delete.class || action == FindDublicates.class) {
            try {
                TransactionStore.addTransaction(action.getConstructor(Node.class).newInstance(node));
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                fileManager.showThrowable(e);
            }
            resetState();
        } else if (action == Rename.class) {
            String name = fileManager.showInputDialog("Please enter new name:", "Rename");
            if (name == null || name.contains("/")) {
                fileManager.showErrorMessage("Invalid name! Renaming aborted.", "Rename");
            } else {
                try {
                    TransactionStore.addTransaction(action.getConstructor(Node.class, String.class).newInstance(node, name));
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    fileManager.showThrowable(e);
                }
                resetState();
            }
        }
    }

    private static void resetState() {
        firstSelection = null;
        for (ResetAction reset : resetActions) {
            reset.execute();
        }
        resetActions.clear();
    }


    private static class ResetAction {
        private final JButton button;
        private final String originalText;

        ResetAction(JButton button) {
            this.button = button;
            this.originalText = button.getText();
        }
        void execute(){
            button.setText(originalText);
        }
    }
}

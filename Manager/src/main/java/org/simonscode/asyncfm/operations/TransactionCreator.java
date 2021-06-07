package org.simonscode.asyncfm.operations;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TransactionCreator {

    private static Node firstSelection;
    private static final ArrayList<ResetAction> resetActions = new ArrayList<>();

    public static void handleClick(Class<? extends Transaction> action, JButton button, Node node, AsyncFMFrame frame) {
        if (action == Copy.class || action == Move.class) {
            if (firstSelection == null) {
                resetActions.add(new ResetAction(button));
                button.setText("Set destination folder");
                firstSelection = node;
            } else {
                try {
                    TransactionStore.addTransaction(action.getConstructor(Node.class, Node.class).newInstance(firstSelection, node));
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    frame.showThrowable(e);
                }
                resetState();
            }
        } else if (action == Delete.class || action == FindDuplicates.class) {
            try {
                TransactionStore.addTransaction(action.getConstructor(Node.class).newInstance(node));
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                frame.showThrowable(e);
            }
            resetState();
        } else if (action == Rename.class || action == CreateFolder.class) {
            String name = frame.showInputDialog("Please enter a name:", "Rename");
            if (name == null || name.contains("/")) {
                frame.showErrorMessage("Invalid name! Renaming aborted.", "Rename");
            } else {
                try {
                    TransactionStore.addTransaction(action.getConstructor(Node.class, String.class).newInstance(node, name));
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    frame.showThrowable(e);
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

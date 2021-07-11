package org.simonscode.asyncfm.gui.filemanager;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.operations.*;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ContextMenu extends JPopupMenu {
    private NodeSource nodeSource;
    private Node nodeToBeCopiedOrMoved;
    private boolean deleteSource;

    public ContextMenu(AsyncFMFrame parent) {
        super();

        createMenuItem("Cut", 't', e -> {
            nodeToBeCopiedOrMoved = nodeSource.getSelectedNode();
            deleteSource = true;
        });
        createMenuItem("Copy", 'C', e -> {
            nodeToBeCopiedOrMoved = nodeSource.getSelectedNode();
            deleteSource = false;
        });
        createMenuItem("Paste", 'P', e -> {
            if (deleteSource) {
                TransactionStore.executeAndLogTransaction(new Move(nodeToBeCopiedOrMoved, nodeSource.getSelectedNode()));
            } else {
                TransactionStore.executeAndLogTransaction(new Copy(nodeToBeCopiedOrMoved, nodeSource.getSelectedNode()));
            }
        });
        createMenuItem("Delete", 'D', e -> TransactionStore.executeAndLogTransaction(new Delete(nodeSource.getSelectedNode())));
        createMenuItem("Rename", 'R', e -> TransactionStore.executeAndLogTransaction(new Rename(nodeSource.getSelectedNode(), parent.showInputDialog("New Name", "Rename"))));
        createMenuItem("Create Folder", 'F', e -> TransactionStore.executeAndLogTransaction(new CreateFolder(nodeSource.getSelectedNode(), parent.showInputDialog("Enter name of directory,", "Name"))));
        createMenuItem("Find Duplicates", 'u', e -> TransactionStore.executeAndLogTransaction(new FindDuplicates(nodeSource.getSelectedNode())));
        createMenuItem("Open as Pie Chart", 'P', e -> parent.openPieChart(nodeSource.getSelectedNode()));
    }

    private void createMenuItem(String name, char mnemonic, ActionListener listener) {
        JMenuItem findDuplicates = new JMenuItem(name);
        findDuplicates.setMnemonic(mnemonic);
        findDuplicates.addActionListener(listener);
        add(findDuplicates);
    }

    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }
}

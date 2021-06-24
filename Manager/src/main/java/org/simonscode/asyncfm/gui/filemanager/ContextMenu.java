package org.simonscode.asyncfm.gui.filemanager;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.operations.*;

import javax.swing.*;

public class ContextMenu extends JPopupMenu {
    private NodeSource nodeSource;
    private Node nodeToBeCopiedOrMoved;
    private boolean deleteSource;

    public ContextMenu(AsyncFMFrame parent) {
        super();

        JMenuItem cutFile = new JMenuItem("Cut");
        cutFile.setMnemonic('t');
        cutFile.addActionListener(e -> {
            nodeToBeCopiedOrMoved = nodeSource.getSelectedNode();
            deleteSource = true;
        });
        add(cutFile);

        JMenuItem copyFile = new JMenuItem("Copy");
        copyFile.setMnemonic('C');
        copyFile.addActionListener(e -> {
            nodeToBeCopiedOrMoved = nodeSource.getSelectedNode();
            deleteSource = false;
        });
        add(copyFile);

        JMenuItem pasteFile = new JMenuItem("Paste");
        pasteFile.setMnemonic('P');
        pasteFile.addActionListener(e -> {
            if (deleteSource) {
                TransactionStore.executeAndLogTransaction(new Move(nodeToBeCopiedOrMoved, nodeSource.getSelectedNode()));
            } else {
                TransactionStore.executeAndLogTransaction(new Copy(nodeToBeCopiedOrMoved, nodeSource.getSelectedNode()));
            }
        });
        add(pasteFile);

        JMenuItem findDuplicates = new JMenuItem("Find Duplicates");
        findDuplicates.setMnemonic('u');
        findDuplicates.addActionListener(e -> TransactionStore.executeAndLogTransaction(new FindDuplicates(nodeSource.getSelectedNode())));
        add(findDuplicates);

        JMenuItem renameFile = new JMenuItem("Rename");
        renameFile.setMnemonic('R');
        renameFile.addActionListener(e -> TransactionStore.executeAndLogTransaction(new Rename(nodeSource.getSelectedNode(), parent.showInputDialog("New Name", "Rename"))));
        add(renameFile);

        JMenuItem deleteFile = new JMenuItem("Delete");
        deleteFile.setMnemonic('D');
        deleteFile.addActionListener(e -> TransactionStore.executeAndLogTransaction(new Delete(nodeSource.getSelectedNode())));
        add(deleteFile);

        JMenuItem createFolder = new JMenuItem("Create Folder");
        createFolder.setMnemonic('F');
        createFolder.addActionListener(e -> TransactionStore.executeAndLogTransaction(new CreateFolder(nodeSource.getSelectedNode(), parent.showInputDialog("Enter name of directory,", "Name"))));
        add(createFolder);
    }

    public void setNodeSource(NodeSource nodeSource) {
        this.nodeSource = nodeSource;
    }
}

package org.simonscode.asyncfm.gui.filemanager;

import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;

public class ContextMenu extends JPopupMenu {
    public ContextMenu(AsyncFMFrame parent) {
        super();

        JMenuItem moveFile = new JMenuItem("Move");
        moveFile.setMnemonic('M');
//        moveFile.addActionListener(actionEvent -> parent.fileAction(Move.class, moveFile));
        add(moveFile);

        JMenuItem findDuplicates = new JMenuItem("Find Duplicates");
        findDuplicates.setMnemonic('D');
//        findDuplicates.addActionListener(actionEvent -> parent.fileAction(FindDuplicates.class, findDuplicates));
        add(findDuplicates);

        JMenuItem copyFile = new JMenuItem("Copy");
        copyFile.setMnemonic('C');
//        copyFile.addActionListener(actionEvent -> parent.fileAction(Copy.class, copyFile));
        add(copyFile);

        JMenuItem renameFile = new JMenuItem("Rename");
        renameFile.setMnemonic('R');
//        renameFile.addActionListener(actionEvent -> parent.fileAction(Rename.class, renameFile));
        add(renameFile);

        JMenuItem deleteFile = new JMenuItem("Delete");
        deleteFile.setMnemonic('D');
//        deleteFile.addActionListener(actionEvent -> parent.fileAction(Delete.class, deleteFile));
        add(deleteFile);

        JMenuItem createFolder = new JMenuItem("Create Folder");
        createFolder.setMnemonic('F');
//        createFolder.addActionListener(actionEvent -> parent.fileAction(CreateFolder.class, createFolder));
        add(createFolder);
    }
}

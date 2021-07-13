package org.simonscode.asyncfm.gui.filebrowser.right.table;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.gui.filebrowser.ContextMenu;
import org.simonscode.asyncfm.gui.filebrowser.FileTreeUpdateListener;
import org.simonscode.asyncfm.gui.filebrowser.FolderOpenedListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FileTablePanel extends JPanel implements FolderOpenedListener, FileTreeUpdateListener {

    private final FileDetailsSubPanel detailsPanel;
    private final FileTableSubPanel fileTableSubPanel;

    public FileTablePanel(AsyncFMFrame parent) {
        super(new BorderLayout(3, 3));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        detailsPanel = new FileDetailsSubPanel(this);

        ContextMenu tableContextMenu = new ContextMenu(parent);
        fileTableSubPanel = new FileTableSubPanel(this, tableContextMenu);
        tableContextMenu.setNodeSource(fileTableSubPanel);

        add(fileTableSubPanel, BorderLayout.CENTER);
        add(detailsPanel, BorderLayout.SOUTH);
    }

    public void onFileSelected(Node node) {
        detailsPanel.setFileDetails(node);
    }

    @Override
    public void onFolderOpened(Node node) {
        fileTableSubPanel.showChildrenInTable(node);
        detailsPanel.setFileDetails(node);
    }

    @Override
    public void onNewRootNode(Node rootNode) {
        fileTableSubPanel.showChildrenInTable(rootNode);
        detailsPanel.setFileDetails(rootNode);
    }

    @Override
    public void onFileTreeUpdated() {
        repaint();
    }
}

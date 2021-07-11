package org.simonscode.asyncfm.gui.filemanager;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.NodeWalker;
import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileManagerPanel extends JPanel {
    private final AsyncFMFrame parent;
    private final FileDetailsPanel detailsPanel;
    private final FileTreePanel fileTreePanel;
    private final FileTablePanel fileTablePanel;

    private NodeWalker walker;
    private Node rootNode;

    public FileManagerPanel(AsyncFMFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout(3, 3));
        setBorder(new EmptyBorder(5, 5, 5, 5));

        detailsPanel = new FileDetailsPanel(this);
        ContextMenu treeContextMenu = new ContextMenu(parent);
        fileTreePanel = new FileTreePanel(this, treeContextMenu);
        treeContextMenu.setNodeSource(fileTreePanel);

        ContextMenu tableContextMenu = new ContextMenu(parent);
        fileTablePanel = new FileTablePanel(rootNode, this, tableContextMenu);
        tableContextMenu.setNodeSource(fileTablePanel);

        JPanel rightHalf = new JPanel(new BorderLayout(3, 3));
        rightHalf.add(fileTablePanel, BorderLayout.CENTER);
        rightHalf.add(detailsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                fileTreePanel,
                rightHalf);
        add(splitPane, BorderLayout.CENTER);
    }

    public void onFileSelected(Node node) {
        detailsPanel.setFileDetails(node);
    }

    public void onFolderOpened(Node node) {
        parent.setCurrentPath(node.getPath());
        fileTreePanel.selectPath(node.getTreePath());
        fileTablePanel.showChildrenInTable(node);
        detailsPanel.setFileDetails(node);
    }

    public void onFileTreeUpdated() {
        fileTreePanel.onFileTreeUpdated();
        fileTablePanel.onFileTreeChanged();
        repaint();
    }

    public void loadFile(File path) throws IOException {
        var fis = new FileInputStream(path);
        walker = new NodeWalker(fis, true, parent);
        rootNode = walker.readTree();
        fileTreePanel.setRootNode(rootNode);
        fileTablePanel.setRootNode(rootNode);
        onFileTreeUpdated();
    }

    public void saveFile(File path) throws IOException {
        var fos = new FileOutputStream(path);
        walker.writeData(rootNode, fos);
    }
}

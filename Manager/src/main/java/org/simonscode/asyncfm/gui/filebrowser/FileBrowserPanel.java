package org.simonscode.asyncfm.gui.filebrowser;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.AsyncFMFrame;
import org.simonscode.asyncfm.gui.filebrowser.left.FileTreePanel;
import org.simonscode.asyncfm.gui.filebrowser.right.piechart.FilePieChartPanel;
import org.simonscode.asyncfm.gui.filebrowser.right.table.FileTablePanel;

import javax.swing.*;
import java.awt.*;

public class FileBrowserPanel extends JPanel implements FolderOpenedListener, FileTreeUpdateListener {
    private final AsyncFMFrame parent;
    private final FileTreePanel fileTreePanel;
    private final FileTablePanel fileTablePanel;
    private final FilePieChartPanel filePieChartPanel;
    private Node rootNode;

    public FileBrowserPanel(AsyncFMFrame parent) {
        super(new BorderLayout());
        this.parent = parent;
        TransactionStore.setFileBrowser(this);

        ContextMenu treeContextMenu = new ContextMenu(parent);
        fileTreePanel = new FileTreePanel(this, treeContextMenu);
        treeContextMenu.setNodeSource(fileTreePanel);

        fileTablePanel = new FileTablePanel(parent);

        filePieChartPanel = new FilePieChartPanel(this);

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        tabbedPane.add("File Table", fileTablePanel);
        tabbedPane.add("Pie Chart", filePieChartPanel);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                fileTreePanel,
                tabbedPane);
        add(splitPane, BorderLayout.CENTER);
    }

    @Override
    public void onFolderOpened(Node node) {
        parent.setCurrentPath(node.getPath());
        fileTreePanel.selectPath(node.getTreePath());
        fileTablePanel.onFolderOpened(node);
        filePieChartPanel.onFolderOpened(node);
    }

    @Override
    public void onNewRootNode(Node rootNode) {
        this.rootNode = rootNode;
        fileTreePanel.onNewRootNode(rootNode);
        fileTablePanel.onNewRootNode(rootNode);
        filePieChartPanel.onNewRootNode(rootNode);
    }

    public void onFileTreeUpdated() {
        fileTreePanel.onFileTreeUpdated();
        fileTablePanel.onFileTreeUpdated();
        filePieChartPanel.onFileTreeUpdated();
    }
}

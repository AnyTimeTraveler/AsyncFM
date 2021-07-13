package org.simonscode.asyncfm.gui.filebrowser.left;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.filebrowser.ContextMenu;
import org.simonscode.asyncfm.gui.filebrowser.FileTreeUpdateListener;
import org.simonscode.asyncfm.gui.filebrowser.FolderOpenedListener;
import org.simonscode.asyncfm.gui.filebrowser.NodeSource;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class FileTreePanel extends JPanel implements NodeSource, FileTreeUpdateListener {
    private final FolderOpenedListener folderOpenedListener;
    private final ContextMenu contextMenu;
    private JTree tree;
    private Node rootNode;
    private DefaultTreeModel treeModel;

    public FileTreePanel(FolderOpenedListener folderOpenedListener, ContextMenu contextMenu) {
        super(new BorderLayout());
        this.folderOpenedListener = folderOpenedListener;
        this.contextMenu = contextMenu;

        init();
    }

    private void init() {
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setComponentPopupMenu(contextMenu);
        tree.setRootVisible(true);
        tree.addTreeSelectionListener(e -> {
            Node node = (Node) e.getPath().getLastPathComponent();
            folderOpenedListener.onFolderOpened(node);
        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent e) {
                Node node = (Node) e.getPath().getLastPathComponent();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {

            }
        });
        tree.setCellRenderer(new NodeTreeCellRenderer());
        tree.expandRow(0);
        tree.setSelectionInterval(0, 0);
        JScrollPane treeScroll = new JScrollPane(tree);

        // as per trashgod tip
//        tree.setVisibleRowCount(15);

        Dimension preferredSize = treeScroll.getPreferredSize();
        Dimension widePreferred = new Dimension(
                200,
                (int) preferredSize.getHeight());
        treeScroll.setPreferredSize(widePreferred);
        add(treeScroll, BorderLayout.CENTER);
    }

    public void selectPath(TreePath treePath) {
        tree.expandPath(treePath);
        tree.setSelectionPath(treePath);
    }

    @Override
    public Node getSelectedNode() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            return null;
        }
        return (Node) selectionPath.getLastPathComponent();
    }

    @Override
    public void onNewRootNode(Node rootNode) {
        this.rootNode = rootNode;
        removeAll();
        init();
        revalidate();
        repaint();
    }

    @Override
    public void onFileTreeUpdated() {
        treeModel.reload(rootNode);
    }
}

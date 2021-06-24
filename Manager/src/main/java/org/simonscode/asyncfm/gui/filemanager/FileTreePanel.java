package org.simonscode.asyncfm.gui.filemanager;

import org.simonscode.asyncfm.data.Node;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

public class FileTreePanel extends JPanel implements NodeSource {
    private final FileManagerPanel parent;
    private final ContextMenu contextMenu;
    private JTree tree;
    private Node rootNode;
    private DefaultTreeModel treeModel;

    public FileTreePanel(FileManagerPanel parent, ContextMenu contextMenu) {
        super(new BorderLayout());
        this.parent = parent;
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
            parent.onFileSelected(node);
        });
        tree.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent e) {
                Node node = (Node) e.getPath().getLastPathComponent();
                parent.onFolderOpened(node);
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

    public void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
        removeAll();
        init();
        revalidate();
        repaint();
    }

    public void selectPath(TreePath treePath) {
        tree.expandPath(treePath);
        tree.setSelectionPath(treePath);
    }

    public void onFileTreeUpdated() {
        treeModel.reload(rootNode);
    }

    @Override
    public Node getSelectedNode() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            return null;
        }
        return (Node) selectionPath.getLastPathComponent();
    }
}

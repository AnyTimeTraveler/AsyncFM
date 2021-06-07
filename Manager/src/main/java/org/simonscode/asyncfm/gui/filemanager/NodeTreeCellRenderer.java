package org.simonscode.asyncfm.gui.filemanager;


import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * A TreeCellRenderer for a File.
 */
class NodeTreeCellRenderer extends DefaultTreeCellRenderer {

    private final JLabel label;

    NodeTreeCellRenderer() {
        label = new JLabel();
        label.setOpaque(true);
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        Node node = (Node) value;
        label.setIcon(node.isDirectory() ? Icons.folderClosedIcon : Icons.fileIcon);
        label.setText(node.getName());
        label.setToolTipText(node.getPath());

        if (selected) {
            if (node.isDirectory()) {
                label.setIcon(Icons.folderOpenIcon);
            }
            label.setBackground(backgroundNonSelectionColor);
            label.setBackground(backgroundSelectionColor);
            label.setForeground(textSelectionColor);
        } else {
            label.setForeground(textNonSelectionColor);
        }
        if (node.isMarked()){
            label.setBackground(Color.MAGENTA);
        }
        return label;
    }
}
package org.simonscode.asyncfm.gui;


import org.simonscode.asyncfm.data.Node;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * A TreeCellRenderer for a File.
 */
class NodeTreeCellRenderer extends DefaultTreeCellRenderer {

    private JLabel label;

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
        label.setIcon(node.isDirectory() ? FileManager.folderClosedIcon : FileManager.fileIcon);
        label.setText(node.getName());
        label.setToolTipText(node.getPath());

        if (selected) {
            if (node.isDirectory()) {
                label.setIcon(FileManager.folderOpenIcon);
            }
            label.setBackground(backgroundNonSelectionColor);
            label.setBackground(backgroundSelectionColor);
            label.setForeground(textSelectionColor);
        } else {
            label.setForeground(textNonSelectionColor);
        }
        return label;
    }
}
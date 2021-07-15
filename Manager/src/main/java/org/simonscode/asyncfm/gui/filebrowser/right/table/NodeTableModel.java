package org.simonscode.asyncfm.gui.filebrowser.right.table;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.FileSize;
import org.simonscode.asyncfm.gui.Icons;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * A TableModel to hold File[].
 */
class NodeTableModel extends AbstractTableModel {

    private Node parent;
    private final String[] columns = {
            "Icon",
            "File",
            "Size",
            "Hash",
            "Children"
    };

    NodeTableModel(Node parent) {
        this.parent = parent;
    }

    public Object getValueAt(int row, int column) {
        Node file = getFile(row);
        return switch (column) {
            case 0 -> file.isDirectory() ? Icons.folderClosedIcon : Icons.fileIcon;
            case 1 -> {
                if (file == parent) {
                    yield ".";
                } else if (file == parent.getParent()) {
                    yield "..";
                } else {
                    yield file.getName();
                }
            }
            case 2 -> file.getFileSize();
            case 3 -> file.isDirectory() ? "" : Long.toHexString(file.getHash());
            case 4 -> file.countChildren();
            default -> "Logic Error";
        };
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case 0 -> ImageIcon.class;
            case 2 -> FileSize.class;
            case 4 -> Long.class;
            default -> String.class;
        };
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        if (parent == null) {
            return 0;
        } else if (parent.getParent() == null) {
            return parent.getChildren().size() + 1;
        }
        return parent.getChildren().size() + 2;
    }

    public Node getFile(int row) {
        if (parent.getParent() == null) {
            if (row == 0) {
                return parent;
            }
            return (Node) parent.getChildAt(row - 1);
        }
        return switch (row) {
            case 0 -> parent.getParent();
            case 1 -> parent;
            default -> (Node) parent.getChildAt(row - 2);
        };
    }

    public void setParent(Node parent) {
        this.parent = parent;
        fireTableDataChanged();
    }

    public Node getParent() {
        return parent;
    }
}
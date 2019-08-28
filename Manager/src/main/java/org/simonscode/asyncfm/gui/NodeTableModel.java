package org.simonscode.asyncfm.gui;

import org.simonscode.asyncfm.data.Node;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * A TableModel to hold File[].
 */
class NodeTableModel extends AbstractTableModel {

    private Node parent;
    private String[] columns = {
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
        if (file == null){
            switch (column) {
                case 0:
                    return FileManager.folderClosedIcon;
                case 1:
                    return "..";
                case 2:
                case 4:
                    return 0;
                default:
             return "";
            }
        }
        switch (column) {
            case 0:
                return file.isDirectory() ? FileManager.folderClosedIcon : FileManager.fileIcon;
            case 1:
                return row == 0 ? ".." : row == 1 ? "." : file.getName();
            case 2:
                return file.isDirectory() ? file.getAbsoluteSizeString() : file.getSizeString();
            case 3:
                return file.isDirectory() ? "" : Long.toHexString(file.getHash());
            case 4:
                return file.countChildren();
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 4:
                return Long.class;
            default:
                return String.class;
        }
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return parent.getChildren().size() + 2;
    }

    public Node getFile(int row) {
        switch (row) {
            case 0:
                return (Node) parent.getParent();
            case 1:
                return parent;
            default:
                return (Node) parent.getChildAt(row - 2);
        }
    }

    public void setParent(Node parent) {
        this.parent = parent;
        fireTableDataChanged();
    }

    public Node getParent() {
        return parent;
    }
}
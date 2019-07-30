package org.simonscode.asyncfm.gui;

import org.simonscode.asyncfm.data.Node;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A TableModel to hold File[].
 */
class NodeTableModel extends AbstractTableModel {

    private List<Node> nodes;
    private String[] columns = {
            "Icon",
            "File",
            "Size",
            "Hash",
            "Children"
    };

    NodeTableModel() {
        this(new ArrayList<>());
    }

    NodeTableModel(List<Node> files) {
        this.nodes = files;
    }

    public Object getValueAt(int row, int column) {
        Node file = nodes.get(row);
        switch (column) {
            case 0:
                return file.isDirectory() ? FileManager.folderClosedIcon : FileManager.fileIcon;
            case 1:
                return file.getName();
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
        return nodes.size();
    }

    public Node getFile(int row) {
        return nodes.get(row);
    }

    public void setNodes(List<Node> files) {
        this.nodes = files;
        fireTableDataChanged();
    }
}
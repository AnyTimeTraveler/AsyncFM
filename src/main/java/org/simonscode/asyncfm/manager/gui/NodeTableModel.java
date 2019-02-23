package org.simonscode.asyncfm.manager.gui;

import org.simonscode.asyncfm.common.Node;
import org.simonscode.asyncfm.manager.Manager;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * A TableModel to hold File[].
 */
class NodeTableModel extends AbstractTableModel {

    private Node[] nodes;
    private String[] columns = {
            "Icon",
            "File",
            "Size",
            "Hash",
            "Children"
    };

    NodeTableModel() {
        this(new Node[0]);
    }

    NodeTableModel(Node[] files) {
        this.nodes = files;
    }

    public Object getValueAt(int row, int column) {
        Node file = nodes[row];
        switch (column) {
            case 0:
                return null;
            case 1:
                return file.getName();
            case 2:
                return Manager.humanReadableByteCount(file.getSize(), true);
            case 3:
                return Long.toHexString(file.getHash());
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
        return nodes.length;
    }

    public Node getFile(int row) {
        return nodes[row];
    }

    public void setNodes(Node[] files) {
        this.nodes = files;
        fireTableDataChanged();
    }
}
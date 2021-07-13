package org.simonscode.asyncfm.gui.filebrowser.right.table;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.Icons;
import org.simonscode.asyncfm.gui.filebrowser.FileTreeUpdateListener;
import org.simonscode.asyncfm.gui.filebrowser.NodeSource;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FileTableSubPanel extends JPanel implements NodeSource, FileTreeUpdateListener {
    private final JTable table;
    private final int rowIconPadding = 6;
    private NodeTableModel fileTableModel;
    private boolean cellSizesSet = false;

    public FileTableSubPanel(FileTablePanel parent, JPopupMenu contextMenu) {
        super(new BorderLayout());

        fileTableModel = new NodeTableModel(null);
        table = new JTable(fileTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowVerticalLines(false);
        table.setComponentPopupMenu(contextMenu);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewRow = table.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    Node node = ((NodeTableModel) table.getModel()).getFile(modelRow);
                    if (node != null) {
                        if (e.getClickCount() == 1) {
                            parent.onFileSelected(node);
                        } else if (e.getClickCount() == 2) {
                            parent.onFolderOpened(node);
                        }
                    }
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        Dimension d = tableScroll.getPreferredSize();
        tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
        setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));

        add(tableScroll, BorderLayout.CENTER);
    }

    public void showChildrenInTable(final Node node) {
        if (node.isDirectory()) {
            setTableData(node);
        }
    }

    private void setTableData(final Node parent) {
        // This takes long, so do it in a seperate thread
        SwingUtilities.invokeLater(() -> {
            fileTableModel = new NodeTableModel(parent);
            table.setModel(fileTableModel);
            fileTableModel.setParent(parent);
            if (!cellSizesSet) {
                // ownSize adjustment to better account for icons
                table.setRowHeight(Icons.fileIcon.getIconHeight() + rowIconPadding);

                setColumnWidth(0, -1);
                setColumnWidth(3, 60);
                table.getColumnModel().getColumn(3).setMaxWidth(120);
                setColumnWidth(4, -1);

                cellSizesSet = true;
            }
        });
    }

    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        if (width < 0) {
            // use the preferred width of the header..
            JLabel label = new JLabel((String) tableColumn.getHeaderValue());
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int) preferred.getWidth() + 14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    @Override
    public Node getSelectedNode() {
        int row = table.getSelectionModel().getLeadSelectionIndex();
        if (table.getSelectedRow() != -1) {
            return ((NodeTableModel) table.getModel()).getFile(row);
        }
        return null;
    }

    @Override
    public void onNewRootNode(Node rootNode) {
        fileTableModel = new NodeTableModel(rootNode);
        table.setModel(fileTableModel);
    }

    @Override
    public void onFileTreeUpdated() {
        fileTableModel.fireTableDataChanged();
    }
}

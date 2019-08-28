package org.simonscode.asyncfm.gui;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.operations.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class FileManager {

    /**
     * Main GUI container
     */
    private JPanel gui;

    /* Icons */
    public static ImageIcon fileIcon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemResource("icons/16px/file-solid.png")));
    private DefaultTreeModel treeModel;

    /**
     * Directory listing
     */
    private JTable table;
    public static ImageIcon folderOpenIcon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemResource("icons/16px/folder-open-solid.png")));
    private boolean cellSizesSet = false;
    private int rowIconPadding = 6;
    private final String APP_TITLE = "AsyncFM";
    public static ImageIcon folderClosedIcon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemResource("icons/16px/folder-solid.png")));
    /**
     * File-system tree.
     */
    private JTree tree;
    /**
     * Table model for Nodes.
     */
    private NodeTableModel fileTableModel;
    /**
     * root node.
     */
    private final Node rootNode;

    /* Node controls. */
    private JButton renameFile;
    private JButton findDublicates;
    private JButton copyFile;
    private JButton moveFile;
    private JButton deleteFile;

    /* Node details. */
    private JLabel fileName;
    private JTextField path;
    private JLabel absoluteSize;
    private JLabel ownSize;
    private JLabel hash;

    public FileManager(Node rootNode) {
        this.rootNode = rootNode;
    }

    private Container getGui() {
        if (gui == null) {
            gui = new JPanel(new BorderLayout(3, 3));
            gui.setBorder(new EmptyBorder(5, 5, 5, 5));

            JPanel detailView = new JPanel(new BorderLayout(3, 3));

            table = new JTable();
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setAutoCreateRowSorter(true);
            table.setShowVerticalLines(false);
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    Point point = e.getPoint();
                    int row = table.rowAtPoint(point);
                    if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                        Node node = ((NodeTableModel) table.getModel()).getFile(row);
                        setFileDetails(node);
                        showChildrenInTable(node);
                        final TreePath treePath = node.getTreePath();
                        tree.expandPath(treePath);
                        tree.setSelectionPath(treePath);
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.getSelectionModel().getLeadSelectionIndex();
                    if (row > -1) {
                        setFileDetails(((NodeTableModel) table.getModel()).getFile(row));
                    }
                }
            });
            JScrollPane tableScroll = new JScrollPane(table);
            tableScroll.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setFileDetails(((NodeTableModel) table.getModel()).getFile(1));
                    table.clearSelection();
                }
            });
            Dimension d = tableScroll.getPreferredSize();
            tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
            detailView.add(tableScroll, BorderLayout.CENTER);

            // the Node tree
            treeModel = new DefaultTreeModel(rootNode);

            TreeSelectionListener treeSelectionListener = tse -> {
                Node node = (Node) tse.getPath().getLastPathComponent();
                showChildrenInTable(node);
                setFileDetails(node);
            };

            tree = new JTree(treeModel);
            tree.setRootVisible(false);
            tree.addTreeSelectionListener(treeSelectionListener);
            tree.setCellRenderer(new NodeTreeCellRenderer());
            tree.expandRow(0);
            JScrollPane treeScroll = new JScrollPane(tree);

            // as per trashgod tip
            tree.setVisibleRowCount(15);

            Dimension preferredSize = treeScroll.getPreferredSize();
            Dimension widePreferred = new Dimension(
                    200,
                    (int) preferredSize.getHeight());
            treeScroll.setPreferredSize(widePreferred);

            // details for a Node
            JPanel fileMainDetails = new JPanel(new BorderLayout(4, 2));
            fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));

            JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
            fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

            JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
            fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

            fileDetailsLabels.add(new JLabel("Name", JLabel.TRAILING));
            fileName = new JLabel();
            fileDetailsValues.add(fileName);

            fileDetailsLabels.add(new JLabel("Path", JLabel.TRAILING));
            path = new JTextField(5);
            path.setEditable(false);
            fileDetailsValues.add(path);

            fileDetailsLabels.add(new JLabel("Absolute size", JLabel.TRAILING));
            absoluteSize = new JLabel();
            fileDetailsValues.add(absoluteSize);

            fileDetailsLabels.add(new JLabel("Node size", JLabel.TRAILING));
            ownSize = new JLabel();
            fileDetailsValues.add(ownSize);

            fileDetailsLabels.add(new JLabel("Hash", JLabel.TRAILING));
            hash = new JLabel();
            fileDetailsValues.add(hash);


            int count = fileDetailsLabels.getComponentCount();
            for (int i = 0; i < count; i++) {
                fileDetailsLabels.getComponent(i).setEnabled(false);
            }

            JToolBar toolBar = new JToolBar();
            // mnemonics stop working in a floated toolbar
            toolBar.setFloatable(false);

            setFileDetails(rootNode);
            showChildrenInTable(rootNode);

            moveFile = new JButton("Move");
            moveFile.setMnemonic('m');
            moveFile.addActionListener(actionEvent -> fileAction(Move.class, moveFile));
            toolBar.add(moveFile);

            findDublicates = new JButton("Find Dublicates");
            findDublicates.setMnemonic('d');
            findDublicates.addActionListener(actionEvent -> fileAction(FindDublicates.class, findDublicates));
            toolBar.add(findDublicates);

            copyFile = new JButton("Copy");
            copyFile.setMnemonic('c');
            copyFile.addActionListener(actionEvent -> fileAction(Copy.class, copyFile));
            toolBar.add(copyFile);

            renameFile = new JButton("Rename");
            renameFile.setMnemonic('r');
            renameFile.addActionListener(actionEvent -> fileAction(Rename.class, renameFile));
            toolBar.add(renameFile);

            deleteFile = new JButton("Delete");
            deleteFile.setMnemonic('d');
            deleteFile.addActionListener(actionEvent -> fileAction(Delete.class, deleteFile));
            toolBar.add(deleteFile);

            JPanel fileView = new JPanel(new BorderLayout(3, 3));
            fileView.add(toolBar, BorderLayout.NORTH);
            fileView.add(fileMainDetails, BorderLayout.CENTER);

            detailView.add(fileView, BorderLayout.SOUTH);

            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    treeScroll,
                    detailView);
            gui.add(splitPane, BorderLayout.CENTER);

        }
        return gui;
    }

    private void fileAction(Class<? extends Transaction> action, JButton button) {
        if (table.getSelectedRow() == -1) {
            TransactionCreator.handleClick(action, button, ((NodeTableModel) table.getModel()).getParent(), this);
        } else {
            TransactionCreator.handleClick(action, button, ((NodeTableModel) table.getModel()).getFile(table.getSelectedRow()), this);
        }
    }

    private void setTableData(final Node parent) {
        SwingUtilities.invokeLater(() -> {
            if (fileTableModel == null) {
                fileTableModel = new NodeTableModel(parent);
                table.setModel(fileTableModel);
                TableRowSorter<NodeTableModel> trs = new TableRowSorter<>((NodeTableModel) table.getModel());

                class FileSizeComparator implements Comparator {
                    public int compare(Object o1, Object o2) {
                        FileSize one = (FileSize) o1;
                        FileSize other = (FileSize) o2;
                        return one.compareTo(other);
                    }
                }

                trs.setComparator(2, new FileSizeComparator());
                table.setRowSorter(trs);
            }
            fileTableModel.setParent(parent);
            if (!cellSizesSet) {
                Icon icon = fileIcon;
                // ownSize adjustment to better account for icons
                table.setRowHeight(icon.getIconHeight() + rowIconPadding);

                setColumnWidth(0, -1);
                setColumnWidth(3, 60);
                table.getColumnModel().getColumn(3).setMaxWidth(120);
                setColumnWidth(4, -1);

                cellSizesSet = true;
            }
        });
    }

    private void showRootFile() {
        // ensure the main files are displayed
        tree.setSelectionInterval(0, 0);
    }

    public void showErrorMessage(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(
                gui,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public String showInputDialog(String message, String title) {
        return JOptionPane.showInputDialog(
                gui,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    public void showThrowable(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(
                gui,
                t.toString(),
                t.getMessage(),
                JOptionPane.ERROR_MESSAGE
        );
        gui.repaint();
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

    private void showChildrenInTable(final Node node) {
        if (node.isDirectory()) {
            setTableData(node);
        }
    }

    /**
     * Update the Node details view with the details of this Node.
     */
    private void setFileDetails(Node node) {
        JFrame f = (JFrame) gui.getTopLevelAncestor();
        if (node != null) {
            Icon icon = node.isDirectory() ? folderClosedIcon : fileIcon;
            fileName.setIcon(icon);
            fileName.setText(node.getName());
            path.setText(node.getPath());
            absoluteSize.setText(FileSize.humanReadableByteCount(node.getAbsoluteSize()));
            ownSize.setText(node.getSizeString().toString());
            hash.setText(Long.toHexString(node.getHash()));
            if (f != null) {
                f.setTitle(APP_TITLE + " :: " + node.getPath());
            }
        } else {
            fileName.setText("File not part of the image");
            path.setText("");
            absoluteSize.setText("");
            ownSize.setText("");
            hash.setText("");
            if (f != null) {
                f.setTitle(APP_TITLE);
            }
        }
        gui.repaint();
    }

    public void createAndShowGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Significantly improves the look of the output in
                // terms of the node names returned by FileSystemView!
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            JFrame f = new JFrame(APP_TITLE);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            f.setContentPane(getGui());

            try {
                URL urlBig = getClass().getResource("fm-icon-32x32.png");
                URL urlSmall = getClass().getResource("fm-icon-16x16.png");
                ArrayList<Image> images = new ArrayList<>();
                images.add(ImageIO.read(urlBig));
                images.add(ImageIO.read(urlSmall));
                f.setIconImages(images);
            } catch (Exception ignored) {
            }

            f.pack();
            f.setLocationByPlatform(true);
            f.setMinimumSize(f.getSize());
            f.setVisible(true);

            showRootFile();
        });
    }

    public void update() {
        fileTableModel.fireTableDataChanged();
    }
}

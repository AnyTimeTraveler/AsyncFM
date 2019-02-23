package org.simonscode.asyncfm.manager.gui;

import org.simonscode.asyncfm.common.FileNode;
import org.simonscode.asyncfm.common.Node;
import org.simonscode.asyncfm.manager.Manager;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileManager {

    /* Icons */
    public static ImageIcon fileIcon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("icons/16px/file-solid.png")));
    public static ImageIcon folderOpenIcon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("icons/16px/folder-open-solid.png")));

    /**
     * Main GUI container
     */
    private JPanel gui;

    /**
     * Node-system tree. Built Lazily
     */
    private JTree tree;
    private DefaultTreeModel treeModel;

    /**
     * Directory listing
     */
    private JTable table;
    private JProgressBar progressBar;
    /**
     * Table model for Node[].
     */
    private NodeTableModel fileTableModel;
    private ListSelectionListener listSelectionListener;
    private boolean cellSizesSet = false;
    private int rowIconPadding = 6;
    private final String APP_TITLE = "AsyncFM";
    public static ImageIcon folderClosedIcon = new ImageIcon(Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("icons/16px/folder-solid.png")));
    /**
     * root node.
     */
    private final Node rootNode;
    /**
     * currently selected node.
     */
    private Node currentNode;

    /* Node controls. */
    private JButton openFile;
    private JButton renameFile;
    private JButton deduplicateFile;
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

            listSelectionListener = lse -> {
                int row = table.getSelectionModel().getLeadSelectionIndex();
                setFileDetails(((NodeTableModel) table.getModel()).getFile(row));
            };

            table.getSelectionModel().addListSelectionListener(listSelectionListener);
            JScrollPane tableScroll = new JScrollPane(table);
            Dimension d = tableScroll.getPreferredSize();
            tableScroll.setPreferredSize(new Dimension((int) d.getWidth(), (int) d.getHeight() / 2));
            detailView.add(tableScroll, BorderLayout.CENTER);

            // the Node tree
            DefaultMutableTreeNode root = new DefaultMutableTreeNode();
            treeModel = new DefaultTreeModel(root);

            TreeSelectionListener treeSelectionListener = tse -> {
                DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
                showChildren(node);
                setFileDetails((Node) node.getUserObject());
            };

            // show the file system root.
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(rootNode);
            root.add(node);

            List<Node> files = rootNode.getChildren();
            for (Node file : files) {
                if (file.isDirectory()) {
                    node.add(new DefaultMutableTreeNode(file));
                }
            }

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


            openFile = new JButton("Open");
            openFile.setMnemonic('o');
            toolBar.add(openFile);

            moveFile = new JButton("Move");
            moveFile.setMnemonic('m');
            moveFile.addActionListener(ae -> showErrorMessage("'Move' not implemented.", "Not implemented."));
            toolBar.add(moveFile);

            deduplicateFile = new JButton("Deduplicate");
            deduplicateFile.setMnemonic('u');
            deduplicateFile.addActionListener(ae -> showErrorMessage("'Deduplicate' not implemented.", "Not implemented."));
            toolBar.add(deduplicateFile);

            toolBar.addSeparator();

            copyFile = new JButton("Copy");
            copyFile.setMnemonic('c');
            copyFile.addActionListener(ae -> showErrorMessage("'Copy' not implemented.", "Not implemented."));
            toolBar.add(copyFile);

            renameFile = new JButton("Rename");
            renameFile.setMnemonic('r');
            renameFile.addActionListener(ae -> renameFile());
            toolBar.add(renameFile);

            deleteFile = new JButton("Delete");
            deleteFile.setMnemonic('d');
            deleteFile.addActionListener(ae -> deleteFile());
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

            JPanel simpleOutput = new JPanel(new BorderLayout(3, 3));
            progressBar = new JProgressBar();
            simpleOutput.add(progressBar, BorderLayout.EAST);
            progressBar.setVisible(false);

            gui.add(simpleOutput, BorderLayout.SOUTH);

        }
        return gui;
    }

    private void showRootFile() {
        // ensure the main files are displayed
        tree.setSelectionInterval(0, 0);
    }

    private TreePath findTreePath(Node find) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath treePath = tree.getPathForRow(i);
            Object object = treePath.getLastPathComponent();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) object;
            Node nodeFile = (Node) node.getUserObject();

            if (nodeFile == find) {
                return treePath;
            }
        }
        // not found!
        return null;
    }

    private void renameFile() {
        if (currentNode == null) {
            showErrorMessage("No file selected to rename.", "Select Node");
            return;
        }

        String renameTo = JOptionPane.showInputDialog(gui, "New Name");
        if (renameTo != null) {
            try {
                boolean directory = currentNode.isDirectory();
                TreePath parentPath = findTreePath(currentNode.getParent());
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) parentPath.getLastPathComponent();

//                boolean renamed = currentNode.renameTo(new Node(currentNode.getParentFile(), renameTo));
                boolean renamed = false;
                //TODO: Implement rename!
                //noinspection ConstantConditions
                if (renamed) {
                    updateTree(directory, parentNode);
                } else {
                    String msg = "The file '" +
                            currentNode +
                            "' could not be renamed.";
                    showErrorMessage(msg, "Rename Failed");
                }
            } catch (Throwable t) {
                showThrowable(t);
            }
        }
        gui.repaint();
    }

    private void updateTree(boolean directory, DefaultMutableTreeNode parentNode) {
        if (directory) {
            // rename the node..

            // delete the current node..
            TreePath currentPath = findTreePath(currentNode);
            System.out.println(currentPath);
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) currentPath.getLastPathComponent();

            treeModel.removeNodeFromParent(currentNode);

            // add a new node..
        }

        showChildren(parentNode);
    }

    private void deleteFile() {
        if (currentNode == null) {
            showErrorMessage("No file selected for deletion.", "Select Node");
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                gui,
                "Are you sure you want to delete this file?",
                "Delete Node",
                JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            try {
                System.out.println("currentNode: " + currentNode);
                TreePath parentPath = findTreePath(currentNode.getParent());
                System.out.println("parentPath: " + parentPath);
                DefaultMutableTreeNode parentNode =
                        (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                System.out.println("parentNode: " + parentNode);

                boolean directory = currentNode.isDirectory();
//                boolean deleted = currentNode.delete();
                boolean deleted = false;
                // TODO: Implement delete!
                if (deleted) {
                    updateTree(directory, parentNode);
                } else {
                    String msg = "The file '" +
                            currentNode +
                            "' could not be deleted.";
                    showErrorMessage(msg, "Delete Failed");
                }
            } catch (Throwable t) {
                showThrowable(t);
            }
        }
        gui.repaint();
    }

    private void showErrorMessage(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(
                gui,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showThrowable(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(
                gui,
                t.toString(),
                t.getMessage(),
                JOptionPane.ERROR_MESSAGE
        );
        gui.repaint();
    }

    /**
     * Update the table on the EDT
     */
    private void setTableData(final Node[] files) {
        SwingUtilities.invokeLater(() -> {
            if (fileTableModel == null) {
                fileTableModel = new NodeTableModel();
                table.setModel(fileTableModel);
            }
            table.getSelectionModel().removeListSelectionListener(listSelectionListener);
            fileTableModel.setNodes(files);
            table.getSelectionModel().addListSelectionListener(listSelectionListener);
            if (!cellSizesSet) {
                Icon icon = fileIcon;
                // ownSize adjustment to better account for icons
                table.setRowHeight(icon.getIconHeight() + rowIconPadding);

                System.out.println(table.getRowHeight());

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

    /**
     * Add the files that are contained within the directory of this node.
     * Thanks to Hovercraft Full Of Eels.
     */
    private void showChildren(final DefaultMutableTreeNode node) {
        tree.setEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<Void, Node> worker = new SwingWorker<Void, Node>() {
            @Override
            public Void doInBackground() {
                Node file = (Node) node.getUserObject();
                if (file.isDirectory()) {
                    Node[] files = file.getChildren().toArray(new Node[0]);
                    if (node.isLeaf()) {
                        for (Node child : files) {
                            if (child.isDirectory()) {
                                publish(child);
                            }
                        }
                    }
                    setTableData(files);
                }
                return null;
            }

            @Override
            protected void process(List<Node> chunks) {
                for (Node child : chunks) {
                    node.add(new DefaultMutableTreeNode(child));
                }
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                tree.setEnabled(true);
            }
        };
        worker.execute();
    }

    /**
     * Update the Node details view with the details of this Node.
     */
    private void setFileDetails(Node file) {
        currentNode = file;
        Icon icon = file.isDirectory() ? folderClosedIcon : fileIcon;
        fileName.setIcon(icon);
        fileName.setText(file.getName());
        path.setText(file.getPath());
        absoluteSize.setText(Manager.humanReadableByteCount(file.getAbsoluteSize(), true));
        ownSize.setText(Manager.humanReadableByteCount(file.getSize(), true));
        hash.setText(file.isDirectory() ? "" : Long.toHexString(((FileNode) file).getHash()));

        JFrame f = (JFrame) gui.getTopLevelAncestor();
        if (f != null) {
            f.setTitle(APP_TITLE + " :: " + file.getName());
        }

        gui.repaint();
    }

    public void createAndShowGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Significantly improves the look of the output in
                // terms of the file names returned by FileSystemView!
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
}

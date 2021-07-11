package org.simonscode.asyncfm.gui;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.data.TransactionStore;
import org.simonscode.asyncfm.gui.charts.FilePieChartPanel;
import org.simonscode.asyncfm.gui.filemanager.FileManagerPanel;
import org.simonscode.asyncfm.gui.transactions.TransactionsPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class AsyncFMFrame extends JFrame {
    @SuppressWarnings("FieldCanBeLocal")
    private final String APP_TITLE = "AsyncFM";
    private final FileManagerPanel fileBrowserPanel = new FileManagerPanel(this);
    private final TransactionsPanel transactionsPanel = new TransactionsPanel(this);

    public AsyncFMFrame() throws HeadlessException {
        super("AsyncFM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TransactionStore.setFrame(this);
        loadAndSetIcon();

        setContentPane(new JTabbedPane(SwingConstants.TOP));
        getContentPane().add("File Browser", fileBrowserPanel);
        getContentPane().add("Transactions", transactionsPanel);

        setJMenuBar(createMenuBar());

        pack();
        setLocationByPlatform(true);
        setMinimumSize(getSize());
    }

    public AsyncFMFrame(String path) {
        this();
        try {
            fileBrowserPanel.loadFile(new File(path));
        } catch (IOException e) {
            showThrowable(e);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(a -> {
            JFileChooser fc = createFileChooser();
            fc.showOpenDialog(this);
            if (fc.getSelectedFile() != null) {
                Thread readerThread = new Thread(() -> {
                    try {
                        fileBrowserPanel.loadFile(fc.getSelectedFile());
                    } catch (IOException e) {
                        showThrowable(e);
                    }
                });
                readerThread.setDaemon(true);
                readerThread.start();
            }
        });
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save as...");
        saveItem.addActionListener(a -> {
            JFileChooser fc = createFileChooser();
            fc.showSaveDialog(this);
            if (fc.getSelectedFile() != null) {
                Thread writerThread = new Thread(() -> {
                    try {
                        fileBrowserPanel.saveFile(fc.getSelectedFile());
                    } catch (IOException e) {
                        showThrowable(e);
                    }
                });
                writerThread.setDaemon(true);
                writerThread.start();
            }
        });
        fileMenu.add(saveItem);

        bar.add(fileMenu);

        return bar;
    }

    private JFileChooser createFileChooser() {
        JFileChooser fc = new JFileChooser(new File("."));
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.addChoosableFileFilter(new FileNameExtensionFilter("AsyncFM Files", "adat"));
        return fc;
    }

    private void loadAndSetIcon() {
        try {
            URL urlBig = getClass().getClassLoader().getResource("icons/32px/file-solid.png");
            URL urlSmall = getClass().getClassLoader().getResource("icons/16px/file-solid.png");
            ArrayList<Image> images = new ArrayList<>();
            assert urlBig != null;
            assert urlSmall != null;
            images.add(ImageIO.read(urlBig));
            images.add(ImageIO.read(urlSmall));
            setIconImages(images);
        } catch (Exception ignored) {
        }
    }

    public void showErrorMessage(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(
                this,
                errorMessage,
                errorTitle,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public String showInputDialog(String message, String title) {
        return JOptionPane.showInputDialog(
                this,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    public void showThrowable(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                t.toString(),
                t.getMessage(),
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void setCurrentPath(String path) {
        setTitle(APP_TITLE + " :: " + path);
    }

    public void onFileTreeUpdated() {
        fileBrowserPanel.onFileTreeUpdated();
        transactionsPanel.onFileTreeUpdated();
    }

    public void openPieChart(Node node) {
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane();
        Component added = tabbedPane.add("Pie Chart : " + node.getName(), new FilePieChartPanel(this, node));

        tabbedPane.setSelectedIndex(tabbedPane.indexOfComponent(added));
    }
}

package org.simonscode.asyncfm.gui.filemanager;

import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.Icons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FileDetailsPanel extends JPanel {
    private final JLabel fileName;
    private final JTextField path;
    private final JLabel absoluteSize;
    private final JLabel ownSize;
    private final JLabel hash;

    public FileDetailsPanel(FileManagerPanel parent) {
        super(new BorderLayout(3, 3));
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

        add(fileMainDetails, BorderLayout.CENTER);
    }


    /**
     * Update the Node details view with the details of this Node.
     */
    void setFileDetails(Node node) {
        fileName.setIcon(node.isDirectory() ? Icons.folderClosedIcon : Icons.fileIcon);
        fileName.setText(node.getName());
        path.setText(node.getPath());
        absoluteSize.setText(node.getAbsoluteSize().toString());
//        ownSize.setText(node.getSizeString().toString());
        hash.setText(Long.toHexString(node.getHash()));
        repaint();
    }
}

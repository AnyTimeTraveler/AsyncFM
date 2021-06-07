package org.simonscode.asyncfm.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoadingDialog {
    private final JDialog dlg;
    private final JProgressBar reading;
    private final JLabel labelReading;
    private final JProgressBar identifying;
    private final JLabel labelIdentifying;
    private final JProgressBar parental;
    private final JLabel labelParental;
    private final JProgressBar arranging;
    private final JLabel labelArranging;
    private final long limit;

    public LoadingDialog(long limit) {
        this.limit = limit;
        reading = new JProgressBar(0, (int) limit);
        labelReading = new JLabel("0 / " + limit);
        reading.setLayout(new BorderLayout());
        reading.add(labelReading, BorderLayout.CENTER);

        identifying = new JProgressBar(0, (int) limit);
        labelIdentifying = new JLabel("0 / " + limit);
        identifying.setLayout(new BorderLayout());
        identifying.add(labelIdentifying, BorderLayout.CENTER);

        parental = new JProgressBar(0, (int) limit);
        labelParental = new JLabel("0 / " + limit);
        parental.setLayout(new BorderLayout());
        parental.add(labelParental, BorderLayout.CENTER);

        arranging = new JProgressBar(0, (int) limit);
        labelArranging = new JLabel("0 / " + limit);
        arranging.setLayout(new BorderLayout());
        arranging.add(labelArranging, BorderLayout.CENTER);

        dlg = new JDialog(null, "Loading...", Dialog.ModalityType.TOOLKIT_MODAL);
        dlg.setLayout(new FlowLayout());
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        JPanel panelReading = new JPanel(new BorderLayout());
        panelReading.setPreferredSize(new Dimension(400, 50));
        panelReading.add(BorderLayout.CENTER, reading);
        panelReading.add(BorderLayout.NORTH, new JLabel("Reading file:"));
        dlg.add(panelReading);

        JPanel panelIdentifying = new JPanel(new BorderLayout());
        panelIdentifying.setPreferredSize(new Dimension(400, 50));
        panelIdentifying.add(BorderLayout.CENTER, identifying);
        panelIdentifying.add(BorderLayout.NORTH, new JLabel("Identifying files:"));
        dlg.add(panelIdentifying);

        JPanel panelParental = new JPanel(new BorderLayout());
        panelParental.setPreferredSize(new Dimension(400, 50));
        panelParental.add(BorderLayout.CENTER, parental);
        panelParental.add(BorderLayout.NORTH, new JLabel("Identifying hierarchy:"));
        dlg.add(panelParental);

        JPanel panelArranging = new JPanel(new BorderLayout());
        panelArranging.setPreferredSize(new Dimension(400, 50));
        panelArranging.add(BorderLayout.CENTER, arranging);
        panelArranging.add(BorderLayout.NORTH, new JLabel("Arranging hierarchy:"));
        dlg.add(panelArranging);

        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dlg.setSize(450, 265);
    }

    public void setProgress(int step, long current) {
        String text = current + " / " + limit;
        switch (step) {
            case 0:
                reading.setValue((int) current);
                labelReading.setText(text);
                break;
            case 1:
                identifying.setValue((int) current);
                labelIdentifying.setText(text);
                break;
            case 2:
                parental.setValue((int) current);
                labelParental.setText(text);
                break;
            case 3:
                arranging.setValue((int) current);
                labelArranging.setText(text);
            default:
                break;
        }
    }

    public void show(AsyncFMFrame parent) {
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    public void close() {
        dlg.setVisible(false);
    }
}

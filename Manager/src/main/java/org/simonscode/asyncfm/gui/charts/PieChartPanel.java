package org.simonscode.asyncfm.gui.charts;

import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PieChartPanel extends JPanel {
    public PieChartPanel() {
        DefaultPieDataset dataset = createDataset();

        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.white);
        chartPanel.addChartMouseListener(new ChartMouseListener() {
            private Comparable lastKey;

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                if (event.getTrigger().getButton() == MouseEvent.BUTTON2) {
                    // TODO: Open context menu
                }
                System.out.println("Clicked: " + event.getEntity());
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                ChartEntity entity = event.getEntity();
                if (entity instanceof PieSectionEntity) {
                    PieSectionEntity section = (PieSectionEntity) entity;
                    PiePlot plot = (PiePlot) chart.getPlot();
                    if (lastKey != null) {
                        plot.setExplodePercent(lastKey, 0);
                    }
                    Comparable key = section.getSectionKey();
                    plot.setExplodePercent(key, 0.05);
                    lastKey = key;
                }
            }
        });
        add(chartPanel);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setContentPane(new PieChartPanel());
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private DefaultPieDataset createDataset() {

        var dataset = new DefaultPieDataset();
        dataset.setValue("Apache", 52);
        dataset.setValue("Nginx", 31);
        dataset.setValue("IIS", 12);
        dataset.setValue("LiteSpeed", 2);
        dataset.setValue("Google server", 1);
        dataset.setValue("Others", 2);

        return dataset;
    }

    private JFreeChart createChart(DefaultPieDataset dataset) {
        return ChartFactory.createPieChart(
                "Web servers market share",
                dataset,
                false, true, false);
    }
}

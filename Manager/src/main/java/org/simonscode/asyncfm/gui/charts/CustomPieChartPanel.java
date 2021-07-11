package org.simonscode.asyncfm.gui.charts;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class CustomPieChartPanel extends ChartPanel {
    public CustomPieChartPanel(JFreeChart chart) {
        super(chart,
                1024, 1024,
                200, 200,
                8192, 8192,
                true,
                false, false, false, false, false, false);
    }
}

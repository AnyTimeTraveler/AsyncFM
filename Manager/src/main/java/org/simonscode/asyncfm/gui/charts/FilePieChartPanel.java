package org.simonscode.asyncfm.gui.charts;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.AsyncFMFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FilePieChartPanel extends JPanel implements ChartMouseListener, ActionListener {

    private final CustomPieChartPanel chartPanel;
    private final JButton showParentFolderButton;
    private final AsyncFMFrame parentFrame;
    private List<Node> nodes = new ArrayList<>();
    private Node selectedNode;
    private Comparable lastKey;

    public FilePieChartPanel(AsyncFMFrame parentFrame, Node selectedNode) {
        super(new BorderLayout());
        this.parentFrame = parentFrame;
        this.selectedNode = selectedNode;

        chartPanel = new CustomPieChartPanel(createPieChart(this.selectedNode));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setFillZoomRectangle(false);
        chartPanel.addChartMouseListener(this);
        add(chartPanel, BorderLayout.CENTER);

        showParentFolderButton = new JButton("Show Parent Folder");
        showParentFolderButton.setEnabled(selectedNode.getParent() != null);
        showParentFolderButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(showParentFolderButton);
        add(buttonPanel, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Node parentNode = selectedNode.getParent();
        if (parentNode != null) {
            selectedNode = parentNode;
            setParentNode();
        }
    }

    private void setParentNode() {
        showParentFolderButton.setEnabled(selectedNode.getParent() != null);
        chartPanel.setChart(createPieChart(selectedNode));
        setName(selectedNode.getPath());
        parentFrame.setCurrentPath(selectedNode.getPath());
    }

    private JFreeChart createPieChart(Node parent) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        Enumeration<Node> iter = (Enumeration<Node>) parent.children();

        List<Node> children = new ArrayList<>();

        while (iter.hasMoreElements()) {
            children.add(iter.nextElement());
        }

        long maxSize = 0;

        for (Node child : children) {
            if (child.getAbsoluteSizeBytes() > maxSize) {
                maxSize = child.getAbsoluteSizeBytes();
            }
        }

        double maxSizeDouble = maxSize;

        HashMap<Node, Double> map = new HashMap<>();

        for (Node child : children) {
            map.put(child, ((double) child.getAbsoluteSizeBytes()) / maxSizeDouble);
        }

        nodes = map.entrySet()
                .stream()
                .sequential()
                .sorted(Map.Entry.comparingByValue())
                .peek(child -> dataset.setValue(child.getKey().getName() + " " + child.getKey().getAbsoluteSize().toString(), child.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return ChartFactory.createPieChart(
                parent.getPath(),
                dataset,
                false,
                true,
                false);
    }

//    @Override
//    public Dimension getSize() {
//        if (this.getHeight() > this.getWidth()) {
//            return new Dimension(getWidth(), getWidth());
//        } else {
//            return new Dimension(getHeight(), getHeight());
//        }
//    }

    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        ChartEntity entity = event.getEntity();
        if (entity instanceof PieSectionEntity) {
            PieSectionEntity section = (PieSectionEntity) entity;

            switch (event.getTrigger().getButton()) {
                case MouseEvent.BUTTON1:
                    selectedNode = nodes.get(section.getSectionIndex());
                    setParentNode();
                    break;
                case MouseEvent.BUTTON2:
                    // TODO: Open context menu
                    break;
            }
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
        ChartEntity entity = event.getEntity();
        if (entity instanceof PieSectionEntity) {
            PieSectionEntity section = (PieSectionEntity) entity;
            PiePlot plot = (PiePlot) chartPanel.getChart().getPlot();
            if (lastKey != null) {
                plot.setExplodePercent(lastKey, 0);
            }
            Comparable key = section.getSectionKey();
            plot.setExplodePercent(key, 0.05);
            lastKey = key;
        }
    }
}

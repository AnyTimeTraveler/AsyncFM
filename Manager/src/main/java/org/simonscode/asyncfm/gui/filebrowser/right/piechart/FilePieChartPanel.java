package org.simonscode.asyncfm.gui.filebrowser.right.piechart;

import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.simonscode.asyncfm.data.Node;
import org.simonscode.asyncfm.gui.filebrowser.FileTreeUpdateListener;
import org.simonscode.asyncfm.gui.filebrowser.FolderOpenedListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FilePieChartPanel extends JPanel implements ChartMouseListener, ActionListener, FileTreeUpdateListener, FolderOpenedListener {
    public static final int MAX_PIE_CHART_SLICES = 50;
    private final FolderOpenedListener folderOpenedListener;
    private final ChartPanel chartPanel;
    private final JButton showParentFolderButton;
    private Node selectedNode;
    @SuppressWarnings("rawtypes")
    private Comparable lastKey;
    private List<Node> nodes = new ArrayList<>();

    public FilePieChartPanel(FolderOpenedListener folderOpenedListener) {
        super(new BorderLayout());
        this.folderOpenedListener = folderOpenedListener;

        chartPanel = new ChartPanel(null,
                1024, 1024,
                200, 200,
                8192, 8192,
                true,
                false, false, false, false, false, false);

        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setFillZoomRectangle(false);
        chartPanel.addChartMouseListener(this);

        add(chartPanel, BorderLayout.CENTER);

        showParentFolderButton = new JButton("Show Parent Folder");
        showParentFolderButton.setEnabled(false);
        showParentFolderButton.addActionListener(this);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(showParentFolderButton);
        add(buttonPanel, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Node parentNode = selectedNode.getParent();
        showParentFolderButton.setEnabled(parentNode != null);
        if (parentNode != null) {
            folderOpenedListener.onFolderOpened(parentNode);
        }
    }

    private JFreeChart createPieChart(Node parent) {
        DefaultPieDataset dataset = new DefaultPieDataset();

        @SuppressWarnings("unchecked")
        Enumeration<Node> iter = (Enumeration<Node>) parent.children();

        List<Node> children = new ArrayList<>();

        while (iter.hasMoreElements()) {
            children.add(iter.nextElement());
        }

        long maxSize = 0;

        for (Node child : children) {
            if (child.getSize() > maxSize) {
                maxSize = child.getSize();
            }
        }

        double maxSizeDouble = maxSize;

        HashMap<Node, Double> map = new HashMap<>();

        for (Node child : children) {
            map.put(child, ((double) child.getSize()) / maxSizeDouble);
        }

        // Get all children into a list ordered by their size percentage of the parent folder
        List<Map.Entry<Node, Double>> tmp = map.entrySet()
                .stream()
                .sequential()
                .sorted(Map.Entry.<Node, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Take the first x elements and add them to the pie chart and also to the list of nodes to be used in the mouse click handler
        nodes = tmp.stream()
                .limit(MAX_PIE_CHART_SLICES)
                .peek(child -> dataset.setValue(child.getKey().getName() + " " + child.getKey().getFileSize().toString(), child.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // To avoid rendering too many nodes, add one big "Rest" Node for all elements that are above the limit of pie slices
        if (map.size() > MAX_PIE_CHART_SLICES) {
            double rest_percentage = tmp.stream()
                    .skip(MAX_PIE_CHART_SLICES)
                    .mapToDouble(Map.Entry::getValue)
                    .sum();
            dataset.setValue("Rest", rest_percentage);
        }

        return ChartFactory.createPieChart(
                parent.getPath(),
                dataset,
                false,
                true,
                false);
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        ChartEntity entity = event.getEntity();
        if (entity instanceof PieSectionEntity) {
            PieSectionEntity section = (PieSectionEntity) entity;

            switch (event.getTrigger().getButton()) {
                case MouseEvent.BUTTON1:
                    if (section.getSectionIndex() < nodes.size()) {
                        folderOpenedListener.onFolderOpened(nodes.get(section.getSectionIndex()));
                    }
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
            @SuppressWarnings("rawtypes")
            Comparable key = section.getSectionKey();
            plot.setExplodePercent(key, 0.1);
            lastKey = key;
        }
    }

    @Override
    public void onFolderOpened(Node node) {
        selectedNode = node;
        showParentFolderButton.setEnabled(selectedNode.getParent() != null);
        chartPanel.setChart(createPieChart(selectedNode));
        chartPanel.setName(selectedNode.getPath());
    }

    @Override
    public void onNewRootNode(Node rootNode) {
        chartPanel.setChart(createPieChart(rootNode));
    }

    @Override
    public void onFileTreeUpdated() {
        chartPanel.setChart(createPieChart(selectedNode));
    }
}

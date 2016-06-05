package ru.spbau.mit.belyaev.gui;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.XYStyler;
import ru.spbau.mit.belyaev.main.MainClient;
import ru.spbau.mit.belyaev.server.Server;

import javax.swing.*;
import java.awt.*;

/**
 * Created by belaevstanislav on 03.06.16.
 * SPBAU Java practice.
 */

class TabbedChart extends JPanel {
    private final static String[] tabsNames = {"Client working time", "Client handling time",
            "Request handling time"};

    private final JTabbedPane tabbedPane;
    private final UILogger uiLogger;

    TabbedChart() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setSize(GUI.WIDTH - 100, GUI.HEIGHT);

        uiLogger = new UILogger();
        final JScrollPane scrollPane = new JScrollPane(uiLogger);
        scrollPane.setPreferredSize(new Dimension(GUI.WIDTH - 100, GUI.HEIGHT));
        tabbedPane.addTab("Logger", scrollPane);

        for (String tabName : tabsNames) {
            final XYChart chart = new XYChart(GUI.WIDTH - 300, GUI.HEIGHT, Styler.ChartTheme.Matlab);
            final XYStyler styler = chart.getStyler();
            styler.setLegendPadding(10);
            styler.setLegendBorderColor(Color.WHITE);
            chart.setYAxisTitle("время, мс");
            final XChartPanel<XYChart> chartPanel = new XChartPanel<>(chart);
            tabbedPane.addTab(tabName, chartPanel);
        }

        add(tabbedPane);
    }

    void drawChartFor(Server.Type serverType, MainClient.TestResult testResult) {
        final String xAxisLabel;
        switch (testResult.getIteratingType()) {
            case CLIENTS:
                xAxisLabel = "количество клиентов";
                break;
            case ARRAY_LENGTH:
                xAxisLabel = "длинна массива";
                break;
            case TIME_DELAY:
                xAxisLabel = "задержка между запросами, мс";
                break;
            case QUERIES:
                xAxisLabel = "количество запросов";
                break;
            default:
                xAxisLabel = "";
                break;
        }

        @SuppressWarnings("unchecked")
        final XChartPanel<XYChart> chartPanel1 = (XChartPanel<XYChart>) tabbedPane.getComponentAt(1);
        chartPanel1.getChart().addSeries(serverType.toString(), testResult.getIteratingValues(),
                testResult.getClientWorkingTimes(), null);
        chartPanel1.getChart().setXAxisTitle(xAxisLabel);

        @SuppressWarnings("unchecked")
        final XChartPanel<XYChart> chartPanel2 = (XChartPanel<XYChart>) tabbedPane.getComponentAt(2);
        chartPanel2.getChart().addSeries(serverType.toString(), testResult.getIteratingValues(),
                testResult.getClientHandlingTimes(), null);
        chartPanel2.getChart().setXAxisTitle(xAxisLabel);

        @SuppressWarnings("unchecked")
        final XChartPanel<XYChart> chartPanel3 = (XChartPanel<XYChart>) tabbedPane.getComponentAt(3);
        chartPanel3.getChart().addSeries(serverType.toString(), testResult.getIteratingValues(),
                testResult.getRequestHandlingTimes(), null);
        chartPanel3.getChart().setXAxisTitle(xAxisLabel);

        uiLogger.logS("*** Update charts ***\n");
    }

    UILogger getUILogger() {
        return uiLogger;
    }
}

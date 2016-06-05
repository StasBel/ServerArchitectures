package ru.spbau.mit.belyaev.gui;

import ru.spbau.mit.belyaev.main.MainClient;
import ru.spbau.mit.belyaev.server.Server;
import ru.spbau.mit.belyaev.util.IntSpan;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 29.05.16.
 * SPBAU Java practice.
 */

public class GUI extends JFrame {
    static final int WIDTH = 1000;
    static final int HEIGHT = 400;

    private static final Logger LOGGER = Logger.getLogger(GUI.class.getName());
    private final static String NAME = "ServerArchitectures";
    private final static Server.Type[] ALL_SERVERS_TYPES = {Server.Type.UDP_FOR_EACH_THREAD, Server.Type.TCP_NON_BLOCKING,
            Server.Type.TCP_FOR_EACH_THREAD, Server.Type.TCP_THREAD_POOL, Server.Type.TCP_SINGLE_THREAD,
            Server.Type.UDP_THREAD_POOL};
    private final static Supplier<IntSpan[]> TEST_LENGTH = () -> new IntSpan[]{
            new IntSpan(15), // clients
            new IntSpan(100, 2000, 300), // length
            new IntSpan(5), // delay
            new IntSpan(15) //queries
    };
    private final static Supplier<IntSpan[]> TEST_CLIENTS = () -> new IntSpan[]{
            new IntSpan(5, 100, 5), // clients
            new IntSpan(100), // length
            new IntSpan(10), // delay
            new IntSpan(10) //queries
    };
    private final static Supplier<IntSpan[]> TEST_DELAY = () -> new IntSpan[]{
            new IntSpan(10), // clients
            new IntSpan(500), // length
            new IntSpan(0, 500, 50), // delay
            new IntSpan(10) //queries
    };
    private final UILogger uiLogger;
    private final MainClient mainClient;
    private final TabbedChart tabbedChart;

    private GUI(String ipAddress) throws HeadlessException, IOException {
        super(NAME);

        // EXIT
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // CHART
        tabbedChart = new TabbedChart();
        add(tabbedChart, BorderLayout.CENTER);
        uiLogger = tabbedChart.getUILogger();

        // SIZE
        setSize(WIDTH, HEIGHT + 100);
        setResizable(false);

        // FINISH
        setVisible(true);

        mainClient = new MainClient(ipAddress);
        uiLogger.logS("Connected to server\n");
    }

    public static void main(String[] args) {
        try {
            // final String ipAddress = "localhost";
            final String ipAddress = "192.168.211.81"; // misha's notebook
            final Server.Type[] serverTypes = {Server.Type.ALL};

            final GUI gui = new GUI(ipAddress);

            gui.performTest(serverTypes, TEST_CLIENTS);

            gui.close();

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("fail connect to server");
        }
    }

    private void close() throws IOException {
        mainClient.stop();
        uiLogger.logS("Disconnected form server\n");
    }

    private void performTest(Server.Type[] serverTypes, Supplier<IntSpan[]> testSupplier) throws IOException {
        if (serverTypes[0] == Server.Type.ALL) {
            serverTypes = ALL_SERVERS_TYPES;
        }

        for (Server.Type serverType : serverTypes) {
            final MainClient.TestResult testResult = mainClient.test(serverType, testSupplier.get(), uiLogger);

            tabbedChart.drawChartFor(serverType, testResult);
        }

        uiLogger.toFile("testing_result.txt");
        uiLogger.logS("Write testing result to file\n");
    }
}

package ru.spbau.mit.belyaev.main;

import ru.spbau.mit.belyaev.client.Client;
import ru.spbau.mit.belyaev.client.ClientFactory;
import ru.spbau.mit.belyaev.gui.UILogger;
import ru.spbau.mit.belyaev.server.Server;
import ru.spbau.mit.belyaev.util.IntSpan;
import ru.spbau.mit.belyaev.util.Stat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 29.05.16.
 * SPBAU Java practice.
 */

public class MainClient {
    private static final Logger LOGGER = Logger.getLogger(MainClient.class.getName());
    private static final int THREADS_NUMBER = 4;

    private final Socket socket;
    private final String ipAddress;

    public MainClient(String ipAddress) throws IOException {
        this.ipAddress = ipAddress;
        socket = new Socket(InetAddress.getByName(ipAddress), MainServer.MAIN_SERVER_PORT_NUMBER);
    }

    public static void main(String[] args) {
        try {
            final String ipAddress = "localhost";
            final Server.Type serverType = Server.Type.UDP_FOR_EACH_THREAD;

            final MainClient mainClient = new MainClient(ipAddress);

            /*mainClient.test(serverType,
                    new IntSpan(20),
                    new IntSpan(300, 2000, 300),
                    new IntSpan(5),
                    new IntSpan(20));*/

            mainClient.stop();

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("Fail to run main client!");
        }
    }

    public TestResult test(Server.Type serverType, IntSpan[] intSpansSet, UILogger logger) throws IOException {
        return test(serverType, intSpansSet[0], intSpansSet[1], intSpansSet[2], intSpansSet[3], logger);
    }

    private TestResult test(Server.Type serverType,
                            IntSpan clientsNumberSpan,
                            IntSpan arrayLengthSpan,
                            IntSpan timeDelaySpan,
                            IntSpan queriesCountSpan,
                            UILogger logger) throws IOException {
        IteratingType iteratingType = IteratingType.CLIENTS;
        IntSpan iteratingSpan = clientsNumberSpan;

        if (arrayLengthSpan.isChange()) {
            iteratingType = IteratingType.ARRAY_LENGTH;
            iteratingSpan = arrayLengthSpan;
        } else if (timeDelaySpan.isChange()) {
            iteratingType = IteratingType.TIME_DELAY;
            iteratingSpan = timeDelaySpan;
        } else if (queriesCountSpan.isChange()) {
            iteratingType = IteratingType.QUERIES;
            iteratingSpan = queriesCountSpan;
        }

        final TestResult result = new TestResult(iteratingType);

        logger.logF("Start testing for server type: " + serverType.toString()
                + ", iterating through " + iteratingType.toString()
                + " from " + iteratingSpan.getFrom()
                + " to " + iteratingSpan.getTo()
                + " with step=" + iteratingSpan.getStep() + "\n");

        while (clientsNumberSpan.hasNext() || arrayLengthSpan.hasNext()
                || timeDelaySpan.hasNext() || queriesCountSpan.hasNext()) {
            final int clientsNumber = clientsNumberSpan.next();
            final int arrayLength = arrayLengthSpan.next();
            final int timeDelay = timeDelaySpan.next();
            final int queriesCount = queriesCountSpan.next();

            logger.logF("Start testing round with clientsNumber=" + clientsNumber
                    + ", arrayLength=" + arrayLength
                    + ", timeDelay=" + timeDelay
                    + ", queriesCount=" + queriesCount + "\n");

            // final ExecutorService threadPool = Executors.newCachedThreadPool();
            // final ExecutorService threadPool = Executors.newFixedThreadPool(THREADS_NUMBER);
            final ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(THREADS_NUMBER);

            startTestServer(serverType);

            final Supplier<Client> clientFactory = ClientFactory.buildClientFactory(serverType, ipAddress,
                    arrayLength, timeDelay, queriesCount);

            final Stat clientWorkingStat = new Stat();
            for (int i = 0; i < clientsNumber; i++) {
                threadPool.submit(() -> {
                    final Client client;

                    try {
                        client = clientFactory.get();
                        client.doQueries(threadPool, clientWorkingStat);
                    } catch (IOException e) {
                        LOGGER.info("Bad I/O!");
                        e.printStackTrace();
                    }
                });
            }

            while (clientWorkingStat.getCount() != clientsNumber) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            threadPool.shutdown();

            final StopServerAnswer stopServerAnswer = stopTestServer();

            logger.logF("Finish testing round, stats:\n");
            logger.logF("Average client working time: " + clientWorkingStat.calcAverageDouble() + " ms\n");
            logger.logF("Average client handling time: " + stopServerAnswer.clientTime + " ms\n");
            logger.logF("Average request handling time: " + stopServerAnswer.requestTime + " ms\n");

            if (iteratingSpan.hasNext()) {
                logger.logF("----------------------------------------------\n");
            }

            result.addRound(clientsNumber, arrayLength, timeDelay, queriesCount,
                    clientWorkingStat.calcAverageDouble(), stopServerAnswer.clientTime, stopServerAnswer.requestTime);
        }

        logger.logF("Finish testing for server type: " + serverType.toString() + "\n");

        return result;
    }

    public void stop() throws IOException {
        socket.close();
    }

    private boolean startTestServer(Server.Type serverType) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        dataOutputStream.writeInt(MainServer.RequestType.START_SERVER.ordinal());
        dataOutputStream.writeInt(serverType.ordinal());
        dataOutputStream.flush();

        return dataInputStream.readByte() == MainServer.OK;
    }

    private StopServerAnswer stopTestServer() throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        dataOutputStream.writeInt(MainServer.RequestType.STOP_SERVER.ordinal());
        dataOutputStream.flush();

        return new StopServerAnswer(
                dataInputStream.readDouble(),
                dataInputStream.readDouble()
        );
    }

    public enum IteratingType { // wil use only three of them
        CLIENTS,
        ARRAY_LENGTH,
        TIME_DELAY,
        QUERIES
    }

    public static class TestResult {
        private final IteratingType iteratingType;

        private final List<Integer> iteratingValues;

        private final List<Double> clientWorkingTimes;
        private final List<Double> clientHandlingTimes;
        private final List<Double> requestHandlingTimes;

        TestResult(IteratingType iteratingType) {
            this.iteratingType = iteratingType;
            iteratingValues = new LinkedList<>();
            clientWorkingTimes = new LinkedList<>();
            clientHandlingTimes = new LinkedList<>();
            requestHandlingTimes = new LinkedList<>();
        }

        private void addRound(int clientsNumber, int arrayLength, int timeDelay, int queriesCount,
                              double clientWorkingTime, double clientHandlingTime, double requestHandlingTime) {
            switch (iteratingType) {
                case CLIENTS:
                    iteratingValues.add(clientsNumber);
                    break;
                case ARRAY_LENGTH:
                    iteratingValues.add(arrayLength);
                    break;
                case TIME_DELAY:
                    iteratingValues.add(timeDelay);
                    break;
                case QUERIES:
                    iteratingValues.add(queriesCount);
                    break;
            }

            clientWorkingTimes.add(clientWorkingTime);
            clientHandlingTimes.add(clientHandlingTime);
            requestHandlingTimes.add(requestHandlingTime);
        }

        public List<Integer> getIteratingValues() {
            return iteratingValues;
        }

        public List<Double> getClientWorkingTimes() {
            return clientWorkingTimes;
        }

        public List<Double> getClientHandlingTimes() {
            return clientHandlingTimes;
        }

        public List<Double> getRequestHandlingTimes() {
            return requestHandlingTimes;
        }

        public IteratingType getIteratingType() {
            return iteratingType;
        }
    }

    private static class StopServerAnswer {
        private final double clientTime;
        private final double requestTime;

        private StopServerAnswer(double clientTime, double requestTime) {
            this.clientTime = clientTime;
            this.requestTime = requestTime;
        }
    }
}

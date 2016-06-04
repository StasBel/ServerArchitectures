package ru.spbau.mit.belyaev.main;

import ru.spbau.mit.belyaev.client.Client;
import ru.spbau.mit.belyaev.client.ClientFactory;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private MainClient(String ipAddress) throws IOException {
        this.ipAddress = ipAddress;
        socket = new Socket(InetAddress.getByName(ipAddress), MainServer.MAIN_SERVER_PORT_NUMBER);
    }

    public static void main(String[] args) {
        try {
            final String ipAddress = "localhost";
            final Server.Type serverType = Server.Type.UDP_FOR_EACH_THREAD;

            final MainClient mainClient = new MainClient(ipAddress);

            mainClient.test(serverType,
                    new IntSpan(20),
                    new IntSpan(300, 2000, 300),
                    new IntSpan(5),
                    new IntSpan(20));

            mainClient.stop();

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("Fail to run main client!");
        }
    }

    private List<TestLapInfo> test(Server.Type serverType,
                                   IntSpan clientsNumberSpan,
                                   IntSpan arrayLengthSpan,
                                   IntSpan timeDelaySpan,
                                   IntSpan queriesCountSpan) throws IOException {
        final List<TestLapInfo> result = new LinkedList<>();

        while (clientsNumberSpan.hasNext() || arrayLengthSpan.hasNext()
                || timeDelaySpan.hasNext() || queriesCountSpan.hasNext()) {
            final int clientsNumber = clientsNumberSpan.next();
            final int arrayLength = arrayLengthSpan.next();
            final int timeDelay = timeDelaySpan.next();
            final int queriesCount = queriesCountSpan.next();

            System.out.println(clientsNumber + " " + arrayLength + " " + timeDelay + " " + queriesCount);

            // final ExecutorService threadPool = Executors.newCachedThreadPool();
            final ExecutorService threadPool = Executors.newFixedThreadPool(THREADS_NUMBER);

            startTestServer(serverType);

            final Supplier<Client> clientFactory = ClientFactory.buildClientFactory(serverType, ipAddress,
                    arrayLength, timeDelay, queriesCount);

            final Stat clientWorkingStat = new Stat();
            for (int i = 0; i < clientsNumber; i++) {
                threadPool.submit(() -> {
                    final Client client;

                    try {
                        client = clientFactory.get();
                        client.doQueries();
                        clientWorkingStat.add(client.getWorkingTime());
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

            System.out.println(clientWorkingStat.calcAverageDouble());
            System.out.println(stopServerAnswer.clientTime);
            System.out.println(stopServerAnswer.requestTime);
            System.out.println("---------------------------");

            result.add(new TestLapInfo(clientsNumber, arrayLength, timeDelay, queriesCount,
                    clientWorkingStat.calcAverageDouble(), stopServerAnswer.clientTime, stopServerAnswer.requestTime));
        }

        LOGGER.info("finish test");

        return result;
    }

    private void stop() throws IOException {
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

    public static class TestLapInfo {
        private final int clientsNumber;
        private final int arrayLength;
        private final int timeDelay;
        private final int queriesCount;

        private final double clientWorkingTime;
        private final double clientHandlingTime;
        private final double requestHandlingTime;

        public TestLapInfo(int clientsNumber, int arrayLength, int timeDelay, int queriesCount,
                           double clientWorkingTime, double clientHandlingTime, double requestHandlingTime) {
            this.clientsNumber = clientsNumber;
            this.arrayLength = arrayLength;
            this.timeDelay = timeDelay;
            this.queriesCount = queriesCount;
            this.clientWorkingTime = clientWorkingTime;
            this.clientHandlingTime = clientHandlingTime;
            this.requestHandlingTime = requestHandlingTime;
        }

        public int getClientsNumber() {
            return clientsNumber;
        }

        public int getArrayLength() {
            return arrayLength;
        }

        public int getTimeDelay() {
            return timeDelay;
        }

        public int getQueriesCount() {
            return queriesCount;
        }

        public double getClientWorkingTime() {
            return clientWorkingTime;
        }

        public double getClientHandlingTime() {
            return clientHandlingTime;
        }

        public double getRequestHandlingTime() {
            return requestHandlingTime;
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

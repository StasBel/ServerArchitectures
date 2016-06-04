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
                    20);

            mainClient.stop();

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("Fail to run main client!");
        }
    }

    private void test(Server.Type serverType,
                      IntSpan clientsNumberSpan,
                      IntSpan arrayLengthSpan,
                      IntSpan timeDelaySpan,
                      int queriesCount) throws IOException {
        while (clientsNumberSpan.hasNext() || arrayLengthSpan.hasNext() || timeDelaySpan.hasNext()) {
            final int clientsNumber = clientsNumberSpan.next();
            final int arrayLength = arrayLengthSpan.next();
            final int timeDelay = timeDelaySpan.next();

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

            System.out.println(clientWorkingStat.calcAverage());
            System.out.println(stopServerAnswer.clientTime);
            System.out.println(stopServerAnswer.requestTime);
            System.out.println("---------------------------");
        }

        LOGGER.info("finish test");
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
                dataInputStream.readLong(),
                dataInputStream.readLong()
        );
    }

    private static class StopServerAnswer {
        private final long clientTime;
        private final long requestTime;

        private StopServerAnswer(long clientTime, long requestTime) {
            this.clientTime = clientTime;
            this.requestTime = requestTime;
        }
    }
}

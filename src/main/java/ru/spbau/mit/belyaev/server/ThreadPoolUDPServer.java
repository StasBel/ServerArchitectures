package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.util.TimeInterval;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

class ThreadPoolUDPServer extends UDPServer {
    private static final Logger LOGGER = Logger.getLogger(ThreadPoolUDPServer.class.getName());
    private static final int THREADS_COUNT = 4;

    private final ExecutorService threadPool;

    ThreadPoolUDPServer(int port) throws SocketException {
        super(port);
        threadPool = Executors.newFixedThreadPool(THREADS_COUNT);
    }

    @Override
    public void start() {
        while (!datagramSocket.isClosed()) {
            try {
                final DatagramPacket datagramPacket = receivePacket();

                final TimeInterval clientTime = new TimeInterval();
                clientTime.start();

                threadPool.submit(() -> {
                    try {
                        handleRequest(datagramPacket, clientTime);
                    } catch (IOException e) {
                        // LOGGER.warning("Finish dealing with connection or exception!");
                    }
                });

            } catch (IOException e) {
                // LOGGER.warning("Connection failed!");
            }
        }
    }

    @Override
    public void stop() throws IOException {
        threadPool.shutdown();
        super.stop();
    }
}

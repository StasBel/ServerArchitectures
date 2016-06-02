package ru.spbau.mit.belyaev.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 25.05.16.
 * SPBAU Java practice.
 */

class ThreadPoolTCPServer extends TCPServer {
    private static final Logger LOGGER = Logger.getLogger(ThreadPoolTCPServer.class.getName());

    private final ExecutorService threadPool;

    ThreadPoolTCPServer(int port) throws IOException {
        super(port);
        threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        while (!serverSocket.isClosed()) {
            try {
                final Socket socket = serverSocket.accept();

                threadPool.submit(() -> {
                    while (!socket.isClosed()) {
                        try {
                            handleRequest(socket);
                        } catch (IOException e) {
                            // LOGGER.warning("Finish dealing with connection or exception!");
                            break;
                        }
                    }
                });

            } catch (IOException e) {
                LOGGER.warning("Connection failed!");
            }
        }
    }

    @Override
    public void stop() throws IOException {
        threadPool.shutdown();
        super.stop();
    }
}

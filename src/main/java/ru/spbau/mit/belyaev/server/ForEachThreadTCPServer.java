package ru.spbau.mit.belyaev.server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 25.05.16.
 * SPBAU Java practice.
 */

class ForEachThreadTCPServer extends TCPServer {
    private static final Logger LOGGER = Logger.getLogger(ForEachThreadTCPServer.class.getName());

    ForEachThreadTCPServer(int port) throws IOException {
        super(port);
    }

    @Override
    public void start() {
        while (!serverSocket.isClosed()) {
            try {
                final Socket socket = serverSocket.accept();

                new Thread(() -> {
                    while (!socket.isClosed()) {
                        try {
                            handleRequest(socket);
                        } catch (IOException e) {
                            LOGGER.warning("Transfer data failed!");
                        }
                    }
                }).start();

            } catch (IOException e) {
                LOGGER.warning("Connection failed!");
            }
        }
    }
}

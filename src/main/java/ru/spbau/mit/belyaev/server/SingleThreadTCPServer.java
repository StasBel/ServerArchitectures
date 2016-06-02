package ru.spbau.mit.belyaev.server;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 24.05.16.
 * SPBAU Java practice.
 */

class SingleThreadTCPServer extends TCPServer {
    private static final Logger LOGGER = Logger.getLogger(SingleThreadTCPServer.class.getName());

    SingleThreadTCPServer(int port) throws IOException {
        super(port);
    }

    @Override
    public void start() {
        while (!serverSocket.isClosed()) {
            try {
                try (final Socket socket = serverSocket.accept()) {
                    handleRequest(socket);
                }
            } catch (IOException e) {
                LOGGER.warning("Connection failed!");
            }
        }
    }
}

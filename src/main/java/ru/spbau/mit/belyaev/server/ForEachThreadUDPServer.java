package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.util.TimeInterval;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 01.06.16.
 * SPBAU Java practice.
 */

class ForEachThreadUDPServer extends UDPServer {
    private static final Logger LOGGER = Logger.getLogger(ForEachThreadUDPServer.class.getName());

    ForEachThreadUDPServer(int port) throws SocketException {
        super(port);
    }

    @Override
    public void start() {
        while (!datagramSocket.isClosed()) {
            try {
                final DatagramPacket datagramPacket = receivePacket();

                final TimeInterval clientTime = new TimeInterval();
                clientTime.start();

                new Thread(() -> {
                    try {
                        handleRequest(datagramPacket, clientTime);
                    } catch (IOException e) {
                        // LOGGER.warning("Finish dealing with connection or exception!");
                    }
                }).start();

            } catch (IOException e) {
                LOGGER.warning("Connection failed!");
            }
        }
    }
}

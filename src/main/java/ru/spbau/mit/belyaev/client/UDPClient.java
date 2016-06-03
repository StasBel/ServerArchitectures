package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import static ru.spbau.mit.belyaev.server.UDPServer.UDP_BUFFER_SIZE;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

class UDPClient extends Client {
    private static final Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private static final int UDP_TIMEOUT = 2000;

    private final byte[] buffer;

    UDPClient(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        super(ipAddress, port, arrayLength, timeDelay, queriesCount);
        buffer = new byte[UDP_BUFFER_SIZE];
    }

    @Override
    public void doQueriesWithoutTime() throws IOException {
        final InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ipAddress), port);
        final DatagramSocket datagramSocket = new DatagramSocket();

        datagramSocket.setSoTimeout(UDP_TIMEOUT);

        /*datagramSocket.setReceiveBufferSize(UDP_BUFFER_SIZE);
        datagramSocket.setSendBufferSize(UDP_BUFFER_SIZE);
        datagramSocket.setSoTimeout(UDP_TIMEOUT);*/

        int alreadyDone = 0;
        while (alreadyDone != queriesCount) {
            // LOGGER.info("start of round");

            final Message.Query query = makeQuery();

            // LOGGER.info("start sending");

            Util.sendQuery(datagramSocket, socketAddress, buffer, query);

            // LOGGER.info("send query");

            final Message.Answer answer = Util.parseAnswer(datagramSocket, buffer);

            // LOGGER.info("parse query");

            if (answer.getCount() != query.getCount()) {
                LOGGER.severe("Got bad answer!");
            }

            alreadyDone++;
            if (alreadyDone != queriesCount) {
                Util.waitForA(timeDelay);
            }

            // LOGGER.info("end of round");
        }

        datagramSocket.close();
    }
}

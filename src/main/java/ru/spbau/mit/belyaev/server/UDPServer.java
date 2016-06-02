package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.TimeInterval;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by belaevstanislav on 01.06.16.
 * SPBAU Java practice.
 */

public abstract class UDPServer extends Server {
    public final static int UDP_BUFFER_SIZE = Short.MAX_VALUE;

    final DatagramSocket datagramSocket;
    private final byte[] buffer;

    UDPServer(int port) throws SocketException {
        super();
        datagramSocket = new DatagramSocket(port);
        buffer = new byte[UDP_BUFFER_SIZE];
    }

    @Override
    public void stop() throws IOException {
        datagramSocket.close();
    }

    DatagramPacket receivePacket() throws IOException {
        final DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagramPacket);
        return datagramPacket;
    }

    void handleRequest(DatagramPacket datagramPacket, TimeInterval clientTime) throws IOException {
        final TimeInterval requestTime = new TimeInterval();

        clientTime.start();
        final Message.Query query = Util.parseQuery(datagramSocket, buffer);

        requestTime.start();
        final Message.Answer answer = handleQueryAndGetAnswer(query);
        requestTime.stop();

        Util.sendAnswer(datagramSocket, datagramPacket.getSocketAddress(), buffer, answer);
        clientTime.stop();

        clientHandlingStat.add(clientTime);
        requestHandlingStat.add(requestTime);
    }
}

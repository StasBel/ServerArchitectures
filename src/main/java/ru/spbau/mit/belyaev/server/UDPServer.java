package ru.spbau.mit.belyaev.server;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.TimeInterval;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Created by belaevstanislav on 01.06.16.
 * SPBAU Java practice.
 */

public abstract class UDPServer extends Server {
    public final static int UDP_BUFFER_SIZE = 15000;

    final DatagramSocket datagramSocket;
    private final byte[] buffer;

    UDPServer(int port) throws SocketException {
        super();
        datagramSocket = new DatagramSocket(port);

        /*datagramSocket.setReceiveBufferSize(UDP_BUFFER_SIZE);
        datagramSocket.setSendBufferSize(UDP_BUFFER_SIZE);
        datagramSocket.setSoTimeout(2000);*/

        buffer = new byte[UDP_BUFFER_SIZE];
    }

    private static Message.Query parseQuery(DatagramPacket datagramPacket) throws InvalidProtocolBufferException {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(datagramPacket.getData());
        final byte[] data = new byte[byteBuffer.getInt()];
        byteBuffer.get(data);
        return Message.Query.parseFrom(data);
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

        final Message.Query query = parseQuery(datagramPacket);

        requestTime.start();
        final Message.Answer answer = handleQueryAndGetAnswer(query);
        requestTime.stop();

        Util.sendAnswer(datagramSocket, datagramPacket.getSocketAddress(), buffer, answer);
        clientTime.stop();

        clientHandlingStat.add(clientTime);
        requestHandlingStat.add(requestTime);
    }
}

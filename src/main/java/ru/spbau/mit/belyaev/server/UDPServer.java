package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;

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

    void handleRequest(DatagramPacket datagramPacket) throws IOException {
        final Message.Query query = Message.Query.parseFrom(datagramPacket.getData());
        final Message.Answer answer = handleQueryAndGetAnswer(query);

        final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.put(answer.toByteArray());
        final DatagramPacket answerDatagramPacket = new DatagramPacket(
                byteBuffer.array(),
                byteBuffer.array().length,
                datagramPacket.getSocketAddress()
        );
        datagramSocket.send(answerDatagramPacket);
    }
}

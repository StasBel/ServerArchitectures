package ru.spbau.mit.belyaev.util;

import ru.spbau.mit.belyaev.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

public class Util {
    private Util() {
    }

    public static void waitForA(long millis) {
        while (millis > 0) {
            final long start = System.currentTimeMillis();

            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            millis -= (System.currentTimeMillis() - start);
        }
    }

    public static void printQuery(Message.Query query) {
        System.out.println(query.getCount());
        System.out.println(Arrays.toString(query.getNumList().toArray()));
    }

    public static void printAnswer(Message.Answer answer) {
        System.out.println(answer.getCount());
        System.out.println(Arrays.toString(answer.getNumList().toArray()));
    }

    public static Message.Query parseQuery(Socket socket) throws IOException { // TCPSocket -> Query
        return Message.Query.parseFrom(getDataFromOneMessageTCP(socket));
    }

    public static Message.Query parseQuery(Socket socket, TimeInterval timeInterval)
            throws IOException { // TCPSocket -> Query with time
        final byte[] data = getDataFromOneMessageTCP(socket);
        timeInterval.start();
        return Message.Query.parseFrom(data);
    }

    public static Message.Answer parseAnswer(Socket socket) throws IOException { // TCPSocket -> Answer
        return Message.Answer.parseFrom(getDataFromOneMessageTCP(socket));
    }

    public static Message.Answer parseAnswer(Socket socket, TimeInterval timeInterval)
            throws IOException { // TCPSocket -> Answer with time
        final byte[] data = getDataFromOneMessageTCP(socket);
        timeInterval.start();
        return Message.Answer.parseFrom(data);
    }

    public static void sendQuery(Socket socket, Message.Query query) throws IOException { // Query -> TCPSocket
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(query.getSerializedSize());
        dataOutputStream.write(query.toByteArray());
        dataOutputStream.flush();
    }

    public static void sendAnswer(Socket socket, Message.Answer answer) throws IOException { // Answer -> TCPSocket
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(answer.getSerializedSize());
        dataOutputStream.write(answer.toByteArray());
        dataOutputStream.flush();
    }

    public static Message.Query parseQuery(DatagramSocket datagramSocket, byte[] buffer) throws IOException { // UDPSocket -> Query
        return Message.Query.parseFrom(getDataFromOneMessageUDP(datagramSocket, buffer));
    }

    public static Message.Query parseQuery(DatagramSocket datagramSocket, byte[] buffer, TimeInterval timeInterval)
            throws IOException { // UDPSocket -> Query with time
        final byte[] data = getDataFromOneMessageUDP(datagramSocket, buffer);
        timeInterval.start();
        return Message.Query.parseFrom(data);
    }

    public static Message.Answer parseAnswer(DatagramSocket datagramSocket, byte[] buffer) throws IOException { // UDPSocket -> Answer
        return Message.Answer.parseFrom(getDataFromOneMessageUDP(datagramSocket, buffer));
    }

    public static Message.Answer parseAnswer(DatagramSocket datagramSocket, byte[] buffer, TimeInterval timeInterval)
            throws IOException { // UDPSocket -> Answer with time
        final byte[] data = getDataFromOneMessageUDP(datagramSocket, buffer);
        timeInterval.start();
        return Message.Answer.parseFrom(data);
    }

    public static void sendQuery(DatagramSocket datagramSocket, SocketAddress socketAddress, byte[] buffer,
                                 Message.Query query) throws IOException { // Query -> UDPSocket
        final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.putInt(query.getSerializedSize());
        byteBuffer.put(query.toByteArray());
        final DatagramPacket datagramPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.array().length,
                socketAddress);
        datagramSocket.send(datagramPacket);
    }

    public static void sendAnswer(DatagramSocket datagramSocket, SocketAddress socketAddress, byte[] buffer,
                                  Message.Answer answer) throws IOException { // Answer -> UDPSocket
        final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
        byteBuffer.putInt(answer.getSerializedSize());
        byteBuffer.put(answer.toByteArray());
        final DatagramPacket datagramPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.array().length,
                socketAddress);
        datagramSocket.send(datagramPacket);
    }

    private static byte[] getDataFromOneMessageTCP(Socket socket) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final int size = dataInputStream.readInt();
        final byte[] data = new byte[size];
        dataInputStream.readFully(data);
        return data;
    }

    private static byte[] getDataFromOneMessageUDP(DatagramSocket datagramSocket, byte[] buffer) throws IOException {
        final DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
        datagramSocket.receive(datagramPacket);
        final ByteBuffer byteBuffer = ByteBuffer.wrap(datagramPacket.getData());
        final byte[] data = new byte[byteBuffer.getInt()];
        byteBuffer.get(data);
        return data;
    }

    public static void waitUntilEnd(ExecutorService threadPool) {
        threadPool.shutdown();
        while (!threadPool.isTerminated()) {
            try {
                threadPool.awaitTermination(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

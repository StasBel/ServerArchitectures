package ru.spbau.mit.belyaev.util;

import ru.spbau.mit.belyaev.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

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
        return Message.Query.parseFrom(getDataFromOneMessage(socket));
    }

    public static Message.Answer parseAnswer(Socket socket) throws IOException { // TCPSocket -> Answer
        return Message.Answer.parseFrom(getDataFromOneMessage(socket));
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

    private static byte[] getDataFromOneMessage(Socket socket) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final int size = dataInputStream.readInt();
        final byte[] data = new byte[size];
        dataInputStream.readFully(data);
        return data;
    }
}

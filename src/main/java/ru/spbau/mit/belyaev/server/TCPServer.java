package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by belaevstanislav on 24.05.16.
 * SPBAU Java practice.
 */

abstract class TCPServer extends Server {
    final ServerSocket serverSocket;

    TCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void stop() throws IOException {
        serverSocket.close();
    }

    void handleRequest(Socket socket) throws IOException {
        final InputStream inputStream = socket.getInputStream();
        final OutputStream outputStream = socket.getOutputStream();

        System.out.println(8);

        // final Message.Query query = Message.Query.parseFrom(inputStream);
        // final Message.Query query = Util.parseQuery(socket);
        final Message.Query query = Message.Query.parseDelimitedFrom(inputStream);

        Util.printQuery(query);

        // final Message.Answer answer = handleQueryAndGetAnswer(query);

        // answer.writeDelimitedTo(outputStream);
        // outputStream.flush();
    }
}

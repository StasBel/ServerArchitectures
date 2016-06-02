package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
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
        final Message.Query query = Util.parseQuery(socket);

        final Message.Answer answer = handleQueryAndGetAnswer(query);

        Util.sendAnswer(socket, answer);
    }
}

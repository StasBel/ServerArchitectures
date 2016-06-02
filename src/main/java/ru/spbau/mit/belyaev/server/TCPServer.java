package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.TimeInterval;
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
        super();
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void stop() throws IOException {
        serverSocket.close();
    }

    void handleRequest(Socket socket) throws IOException {
        final TimeInterval clientTime = new TimeInterval();
        final TimeInterval requestTime = new TimeInterval();

        final Message.Query query = Util.parseQuery(socket, clientTime);

        requestTime.start();
        final Message.Answer answer = handleQueryAndGetAnswer(query);
        requestTime.stop();

        Util.sendAnswer(socket, answer);
        clientTime.stop();

        clientHandlingStat.add(clientTime);
        requestHandlingStat.add(requestTime);
    }
}

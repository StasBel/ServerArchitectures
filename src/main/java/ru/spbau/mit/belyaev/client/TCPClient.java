package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

class TCPClient extends Client {
    private static final Logger LOGGER = Logger.getLogger(TCPClient.class.getName());

    TCPClient(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        super(ipAddress, port, arrayLength, timeDelay, queriesCount);
    }

    @Override
    public void doQueriesWithoutTime() throws IOException {
        final Socket socket = new Socket(InetAddress.getByName(ipAddress), port);

        final InputStream inputStream = socket.getInputStream();
        final OutputStream outputStream = socket.getOutputStream();

        int alreadyDone = 0;
        while (alreadyDone != queriesCount) {
            final Message.Query query = makeQuery();

            Util.printQuery(query);

            query.writeTo(outputStream);
            outputStream.flush();

            final Message.Answer answer = Message.Answer.parseFrom(inputStream);

            if (answer.getCount() != query.getCount()) {
                LOGGER.severe("Got bad response!");
            }

            alreadyDone++;
            if (alreadyDone != queriesCount) {
                Util.waitForA(timeDelay);
            }
        }

        socket.close();
    }
}

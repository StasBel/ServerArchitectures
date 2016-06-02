package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

class SingleThreadTCPClient extends Client {
    private static final Logger LOGGER = Logger.getLogger(SingleThreadTCPClient.class.getName());

    SingleThreadTCPClient(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        super(ipAddress, port, arrayLength, timeDelay, queriesCount);
    }

    @Override
    public void doQueriesWithoutTime() throws IOException {
        int alreadyDone = 0;
        while (alreadyDone != queriesCount) {
            final Socket socket = new Socket(InetAddress.getByName(ipAddress), port);

            final Message.Query query = makeQuery();

            Util.sendQuery(socket, query);

            final Message.Answer answer = Util.parseAnswer(socket);

            if (answer.getCount() != query.getCount()) {
                LOGGER.severe("Got bad answer!");
            }

            socket.close();

            alreadyDone++;
            if (alreadyDone != queriesCount) {
                Util.waitForA(timeDelay);
            }
        }
    }
}

package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Stat;
import ru.spbau.mit.belyaev.util.TimeInterval;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

class TCPClient extends Client {
    private static final Logger LOGGER = Logger.getLogger(TCPClient.class.getName());
    private Socket socket;

    TCPClient(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        super(ipAddress, port, arrayLength, timeDelay, queriesCount);
        socket = null;
    }

    @Override
    public void runRound(TimeInterval workingTime, ScheduledThreadPoolExecutor threadPool,
                         AtomicInteger alreadyDone, Stat clientWorkingStat) {
        try {

            if (socket == null) {
                socket = new Socket(ipAddress, port);
                workingTime.start();
            }

            if (alreadyDone.intValue() == 0) {
                workingTime.start();
            }

            final Message.Query query = makeQuery();

            Util.sendQuery(socket, query);

            final Message.Answer answer = Util.parseAnswer(socket);

            alreadyDone.addAndGet(1);

            if (alreadyDone.intValue() != queriesCount) {
                threadPool.schedule(() -> runRound(workingTime, threadPool, alreadyDone, clientWorkingStat),
                        timeDelay, TimeUnit.MILLISECONDS);
            } else {
                workingTime.stop();
                clientWorkingStat.add(workingTime);

                socket.close();
            }

        } catch (IOException e) {
            LOGGER.warning("exception in runRound");
        }
    }
}

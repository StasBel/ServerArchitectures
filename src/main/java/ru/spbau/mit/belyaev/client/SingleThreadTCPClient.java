package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Stat;
import ru.spbau.mit.belyaev.util.TimeInterval;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
    public void runRound(TimeInterval workingTime, ScheduledThreadPoolExecutor threadPool,
                         AtomicInteger alreadyDone, Stat clientWorkingStat) {
        try {

            final Socket socket = new Socket(InetAddress.getByName(ipAddress), port);

            final Message.Query query = makeQuery();

            Util.sendQuery(socket, query);

            final Message.Answer answer = Util.parseAnswer(socket);

            socket.close();

            alreadyDone.incrementAndGet();

            if (alreadyDone.intValue() != queriesCount) {
                threadPool.schedule(() -> runRound(workingTime, threadPool, alreadyDone, clientWorkingStat),
                        timeDelay, TimeUnit.MILLISECONDS);
            } else {
                workingTime.stop();
                clientWorkingStat.add(workingTime);
            }

        } catch (IOException e) {
            LOGGER.warning("exception in runRound");
        }
    }
}

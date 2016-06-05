package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Stat;
import ru.spbau.mit.belyaev.util.TimeInterval;
import ru.spbau.mit.belyaev.util.Util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static ru.spbau.mit.belyaev.server.UDPServer.UDP_BUFFER_SIZE;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

class UDPClient extends Client {
    private static final Logger LOGGER = Logger.getLogger(UDPClient.class.getName());
    private static final int UDP_TIMEOUT = 10000;

    private final byte[] buffer;
    private InetSocketAddress socketAddress;
    private DatagramSocket datagramSocket;

    UDPClient(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        super(ipAddress, port, arrayLength, timeDelay, queriesCount);
        buffer = new byte[UDP_BUFFER_SIZE];

        socketAddress = null;
        datagramSocket = null;
    }

    @Override
    public void runRound(TimeInterval workingTime, ScheduledThreadPoolExecutor threadPool,
                         AtomicInteger alreadyDone, Stat clientWorkingStat) {
        try {

            if (alreadyDone.intValue() == 0) {
                workingTime.start();
            }

            if (datagramSocket == null) {
                socketAddress = new InetSocketAddress(ipAddress, port);
                datagramSocket = new DatagramSocket();
                datagramSocket.setSoTimeout(UDP_TIMEOUT);
            }

            final Message.Query query = makeQuery();

            Util.sendQuery(datagramSocket, socketAddress, buffer, query);

            final Message.Answer answer = Util.parseAnswer(datagramSocket, buffer);

            alreadyDone.incrementAndGet();

            if (alreadyDone.intValue() != queriesCount) {
                threadPool.schedule(() -> runRound(workingTime, threadPool, alreadyDone, clientWorkingStat),
                        timeDelay, TimeUnit.MILLISECONDS);
            } else {
                workingTime.stop();
                clientWorkingStat.add(workingTime);

                datagramSocket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("exception in runRound");
        }
    }
}

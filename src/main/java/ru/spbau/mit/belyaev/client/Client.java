package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Stat;
import ru.spbau.mit.belyaev.util.TimeInterval;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

public abstract class Client {
    final String ipAddress;
    final int port;
    final long timeDelay;
    final int queriesCount;
    private final int arrayLength;

    Client(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        this.arrayLength = arrayLength;
        this.ipAddress = ipAddress;
        this.port = port;
        this.timeDelay = timeDelay;
        this.queriesCount = queriesCount;
    }

    abstract void runRound(TimeInterval workingTime, ScheduledThreadPoolExecutor threadPool,
                           AtomicInteger alreadyDone, Stat clientWorkingStat);

    public void doQueries(ScheduledThreadPoolExecutor threadPool, Stat clientWorkingStat) throws IOException {
        final TimeInterval workingTime = new TimeInterval();

        final AtomicInteger alreadyDone = new AtomicInteger(0);

        threadPool.schedule(() -> runRound(workingTime, threadPool, alreadyDone, clientWorkingStat),
                0, TimeUnit.MILLISECONDS);
    }

    Message.Query makeQuery() {
        return Message.Query.newBuilder()
                .setCount(arrayLength)
                .addAllNum(new Random().ints(arrayLength).boxed().collect(Collectors.toList()))
                .build();
    }
}

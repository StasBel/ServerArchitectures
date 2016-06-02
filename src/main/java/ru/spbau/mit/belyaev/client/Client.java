package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.TimeInterval;

import java.io.IOException;
import java.util.Random;
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
    private final TimeInterval workingTime;
    private final int arrayLength;

    Client(String ipAddress, int port, int arrayLength, long timeDelay, int queriesCount) {
        this.arrayLength = arrayLength;
        this.ipAddress = ipAddress;
        this.port = port;
        this.timeDelay = timeDelay;
        this.queriesCount = queriesCount;
        workingTime = new TimeInterval();
    }

    abstract void doQueriesWithoutTime() throws IOException;

    public void doQueries() throws IOException {
        workingTime.start();
        doQueriesWithoutTime();
        workingTime.stop();
    }

    public long getWorkingTime() {
        return workingTime.getTimeAbs();
    }

    Message.Query makeQuery() {
        return Message.Query.newBuilder()
                .setCount(arrayLength)
                .addAllNum(new Random().ints(arrayLength).boxed().collect(Collectors.toList()))
                .build();
    }
}

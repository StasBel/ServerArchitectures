package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Stat;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by belaevstanislav on 24.05.16.
 * SPBAU Java practice.
 */

public abstract class Server {
    final Stat clientHandlingStat;
    final Stat requestHandlingStat;

    Server() {
        clientHandlingStat = new Stat();
        requestHandlingStat = new Stat();
    }

    public long getAverageClientHandling() {
        return clientHandlingStat.calcAverage();
    }

    public long getAverageRequestHandling() {
        return requestHandlingStat.calcAverage();
    }

    public abstract void start();

    public abstract void stop() throws IOException;

    Message.Answer handleQueryAndGetAnswer(Message.Query query) {
        return Message.Answer.newBuilder()
                .setCount(query.getCount())
                .addAllNum(query.getNumList().stream().sorted().collect(Collectors.toList()))
                .build();
    }

    public enum Type {
        TCP_FOR_EACH_THREAD,
        TCP_SINGLE_THREAD,
        TCP_THREAD_POOL,
        TCP_NON_BLOCKING,
        UDP_FOR_EACH_THREAD,
        UDP_THREAD_POOL;

        public static Type buildServerType(int ordinal) {
            return values()[ordinal];
        }
    }
}

package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Created by belaevstanislav on 24.05.16.
 * SPBAU Java practice.
 */

public abstract class Server {
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

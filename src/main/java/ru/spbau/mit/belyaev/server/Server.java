package ru.spbau.mit.belyaev.server;

import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.Stat;

import java.io.IOException;

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

    public long getAverageClientHandlingLong() {
        return clientHandlingStat.calcAverageLong();
    }

    public long getAverageRequestHandlingLong() {
        return requestHandlingStat.calcAverageLong();
    }

    public double getAverageClientHandlingDouble() {
        return clientHandlingStat.calcAverageDouble();
    }

    public double getAverageRequestHandlingDouble() {
        return requestHandlingStat.calcAverageDouble();
    }

    public abstract void start();

    public abstract void stop() throws IOException;

    Message.Answer handleQueryAndGetAnswer(Message.Query query) {
        final int newCount = query.getCount();

        Integer[] nums = query.getNumList().stream().toArray(Integer[]::new);

        for (int outer = 0; outer < nums.length - 1; outer++) {
            for (int inner = 0; inner < nums.length - outer - 1; inner++) {
                if (nums[inner] > nums[inner + 1]) {
                    int temp = nums[inner];
                    nums[inner] = nums[inner + 1];
                    nums[inner + 1] = temp;
                }
            }
        }

        final Message.Answer.Builder builder = Message.Answer.newBuilder().setCount(newCount);

        for (int num : nums) {
            builder.addNum(num);
        }

        return builder.build();
    }

    public enum Type {
        TCP_FOR_EACH_THREAD,
        TCP_SINGLE_THREAD,
        TCP_THREAD_POOL,
        TCP_NON_BLOCKING,
        UDP_FOR_EACH_THREAD,
        UDP_THREAD_POOL,
        ALL;

        public static Type buildServerType(int ordinal) {
            return values()[ordinal];
        }
    }
}

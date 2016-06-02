package ru.spbau.mit.belyaev.util;

import ru.spbau.mit.belyaev.Message;

import java.util.Arrays;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

public class Util {
    private Util() {
    }

    public static void waitForA(long millis) {
        while (millis > 0) {
            final long start = System.currentTimeMillis();

            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            millis -= (System.currentTimeMillis() - start);
        }
    }

    public static void printQuery(Message.Query query) {
        System.out.println(query.getCount());
        System.out.println(Arrays.toString(query.getNumList().toArray()));
    }

    public static void printAnswer(Message.Answer answer) {
        System.out.println(answer.getCount());
        System.out.println(Arrays.toString(answer.getNumList().toArray()));
    }
}

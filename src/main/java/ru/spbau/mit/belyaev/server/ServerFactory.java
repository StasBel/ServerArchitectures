package ru.spbau.mit.belyaev.server;

import java.io.IOException;

/**
 * Created by belaevstanislav on 29.05.16.
 * SPBAU Java practice.
 */

public class ServerFactory {
    public static Server buildServer(Server.Type serverType, int port) throws IOException {
        switch (serverType) {
            case TCP_FOR_EACH_THREAD:
                return new ForEachThreadTCPServer(port);
            case TCP_SINGLE_THREAD:
                return new SingleThreadTCPServer(port);
            case TCP_THREAD_POOL:
                return new ThreadPoolTCPServer(port);
            case TCP_NON_BLOCKING:
                return new NonBlockingTCPServer(port);
            case UDP_FOR_EACH_THREAD:
                return new ForEachThreadUDPServer(port);
            case UDP_THREAD_POOL:
                return new ThreadPoolUDPServer(port);
            default:
                return new ForEachThreadTCPServer(port);
        }
    }
}

package ru.spbau.mit.belyaev.client;

import ru.spbau.mit.belyaev.main.MainServer;
import ru.spbau.mit.belyaev.server.Server;

import java.util.function.Supplier;

/**
 * Created by belaevstanislav on 02.06.16.
 * SPBAU Java practice.
 */

public class ClientFactory {
    private ClientFactory() {
    }

    public static Supplier<Client> buildClientFactory(final Server.Type serverType, String ipAddress, int port,
                                                      int arrayLength, long timeDelay, int queriesCount) {
        return () -> getClient(serverType, ipAddress, port, arrayLength, timeDelay, queriesCount);
    }

    public static Supplier<Client> buildClientFactory(final Server.Type serverType, String ipAddress,
                                                      int arrayLength, long timeDelay, int queriesCount) {
        return () -> getClient(serverType, ipAddress, MainServer.TEST_SERVER_PORT_NUMBER,
                arrayLength, timeDelay, queriesCount);
    }

    public static Client buildClient(final Server.Type serverType, String ipAddress, int port,
                                     int arrayLength, long timeDelay, int queriesCount) {
        return getClient(serverType, ipAddress, port, arrayLength, timeDelay, queriesCount);
    }

    public static Client buildClient(final Server.Type serverType, String ipAddress,
                                     int arrayLength, long timeDelay, int queriesCount) {
        return getClient(serverType, ipAddress, MainServer.TEST_SERVER_PORT_NUMBER,
                arrayLength, timeDelay, queriesCount);
    }

    private static Client getClient(final Server.Type serverType, String ipAddress, int port,
                                    int arrayLength, long timeDelay, int queriesCount) {
        switch (serverType) {
            case TCP_FOR_EACH_THREAD:
                return new TCPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
            case TCP_SINGLE_THREAD:
                return new SingleThreadTCPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
            case TCP_THREAD_POOL:
                return new TCPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
            case TCP_NON_BLOCKING:
                return new TCPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
            case UDP_FOR_EACH_THREAD:
                return new UDPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
            case UDP_THREAD_POOL:
                return new UDPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
            default:
                return new TCPClient(ipAddress, port, arrayLength, timeDelay, queriesCount);
        }
    }
}

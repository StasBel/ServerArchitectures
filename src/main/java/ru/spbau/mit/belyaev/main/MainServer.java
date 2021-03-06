package ru.spbau.mit.belyaev.main;

import ru.spbau.mit.belyaev.server.Server;
import ru.spbau.mit.belyaev.server.ServerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 29.05.16.
 * SPBAU Java practice.
 */

public class MainServer {
    public static final int TEST_SERVER_PORT_NUMBER = 3229;
    static final int MAIN_SERVER_PORT_NUMBER = 3230;
    static final byte OK = 0;
    private static final byte BAD = 1;
    private static final Logger LOGGER = Logger.getLogger(MainServer.class.getName());
    private final ServerSocket serverSocket;
    private Server server;

    private MainServer() throws IOException {
        serverSocket = new ServerSocket(MAIN_SERVER_PORT_NUMBER);
    }

    public static void main(String[] args) {
        try {
            final MainServer mainServer = new MainServer();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    mainServer.stop();
                } catch (IOException e) {
                    LOGGER.warning("Fail to stop MainServer!");
                }
            }));

            mainServer.start();
        } catch (IOException e) {
            LOGGER.warning("Fail to run main server!");
        }
    }

    private void start() {
        while (!serverSocket.isClosed()) {
            try {
                final Socket socket = serverSocket.accept();

                while (!socket.isClosed()) {
                    try {
                        handleRequest(socket);
                    } catch (IOException e) {
                        // LOGGER.warning("Finish dealing with connection or exception!");
                        break;
                    }
                }

            } catch (IOException e) {
                // LOGGER.warning("Finish dealing with main client!");
            }
        }
    }

    private void stop() throws IOException {
        serverSocket.close();
    }

    private void handleRequest(Socket socket) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        switch (RequestType.buildRequestType(dataInputStream.readInt())) {
            case START_SERVER:
                final Server.Type serverType = Server.Type.buildServerType(dataInputStream.readInt());
                server = ServerFactory.buildServer(serverType);

                new Thread(() -> {
                    server.start();
                }).start();

                LOGGER.info("Start " + serverType.toString() + " server!");

                dataOutputStream.writeByte(OK);
                dataOutputStream.flush();

                break;
            case STOP_SERVER:
                server.stop();

                LOGGER.info("Stop server!");

                //dataOutputStream.writeLong(server.getAverageClientHandlingLong());
                //dataOutputStream.writeLong(server.getAverageRequestHandlingLong());

                dataOutputStream.writeDouble(server.getAverageClientHandlingDouble());
                dataOutputStream.writeDouble(server.getAverageRequestHandlingDouble());

                dataOutputStream.flush();

                break;
            default:
                dataOutputStream.writeByte(BAD);
                dataOutputStream.flush();

                break;
        }
    }

    enum RequestType {
        START_SERVER, STOP_SERVER;

        public static RequestType buildRequestType(int ordinal) {
            return values()[ordinal];
        }
    }
}

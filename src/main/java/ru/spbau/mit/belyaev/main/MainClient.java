package ru.spbau.mit.belyaev.main;

import ru.spbau.mit.belyaev.client.Client;
import ru.spbau.mit.belyaev.client.ClientFactory;
import ru.spbau.mit.belyaev.server.Server;
import ru.spbau.mit.belyaev.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 29.05.16.
 * SPBAU Java practice.
 */

public class MainClient {
    private static final Logger LOGGER = Logger.getLogger(MainClient.class.getName());

    private final Socket socket;

    private MainClient(String ipAddress) throws IOException {
        socket = new Socket(InetAddress.getByName(ipAddress), MainServer.MAIN_SERVER_PORT_NUMBER);
    }

    public static void main(String[] args) {
        try {
            final MainClient mainClient = new MainClient("localhost");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    mainClient.stopTestServer();
                    mainClient.stop();
                } catch (IOException e) {
                    LOGGER.warning("Fail to stop MainClient or TestServer!");
                }
            }));

            mainClient.startTestServer(Server.Type.TCP_FOR_EACH_THREAD);

            final Client client = ClientFactory.buildClient(Server.Type.TCP_FOR_EACH_THREAD, "localhost", 10, 0, 1);
            client.doQueries();

            final long workingTime = client.getWorkingTime();
            System.out.print(workingTime);

            Util.waitForA(10000);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warning("Fail to run main client!");
        }
    }

    private void stop() throws IOException {
        socket.close();
    }

    private boolean startTestServer(Server.Type serverType) throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        dataOutputStream.writeInt(MainServer.RequestType.START_SERVER.ordinal());
        dataOutputStream.writeInt(serverType.ordinal());
        dataOutputStream.flush();

        return dataInputStream.readByte() == MainServer.OK;
    }

    private boolean stopTestServer() throws IOException {
        final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

        dataOutputStream.writeInt(MainServer.RequestType.STOP_SERVER.ordinal());
        dataOutputStream.flush();

        return dataInputStream.readByte() == MainServer.OK;
    }
}

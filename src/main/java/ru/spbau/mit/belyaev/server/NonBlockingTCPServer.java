package ru.spbau.mit.belyaev.server;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.spbau.mit.belyaev.Message;
import ru.spbau.mit.belyaev.util.TimeInterval;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by belaevstanislav on 30.05.16.
 * SPBAU Java practice.
 */

class NonBlockingTCPServer extends Server {
    private static final Logger LOGGER = Logger.getLogger(NonBlockingTCPServer.class.getName());
    private static final int INTEGER_SIZE = 4;

    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private final ExecutorService threadPool;
    private final Set<SocketChannel> channels;

    NonBlockingTCPServer(int port) throws IOException {
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        threadPool = Executors.newCachedThreadPool();

        channels = new TreeSet<>();
    }

    @Override
    public void start() {
        try {
            while (!serverSocketChannel.isOpen()) {
                selector.select();
                final Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    final SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        accept(key);
                    }

                    if (key.isReadable()) {
                        read(key);
                    }

                    if (key.isWritable()) {
                        write(key);
                    }

                    keyIterator.remove(); // needed to be remove
                }
            }
        } catch (IOException e) {
            // LOGGER.warning("Connection failed!");
        }
    }

    @Override
    public void stop() throws IOException {
        for (SocketChannel channel : channels) {
            channel.close();
        }
        serverSocketChannel.close();
        threadPool.shutdown();
    }

    private void prepareAnswer(RequestContext requestContext) throws InvalidProtocolBufferException {
        final Message.Query query = Message.Query.parseFrom(requestContext.count.put(requestContext.nums).array());

        final TimeInterval requestTime = new TimeInterval();

        requestTime.start();
        final Message.Answer answerPacket = handleQueryAndGetAnswer(query);
        requestTime.stop();

        requestHandlingStat.add(requestTime);

        synchronized (requestContext.lock) {
            requestContext.answerPacket = answerPacket;
        }
    }

    private void accept(SelectionKey key) throws IOException {
        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        final SocketChannel socketChannel = serverSocketChannel.accept();

        socketChannel.configureBlocking(false);
        socketChannel.socket().setTcpNoDelay(true);
        socketChannel.register(
                selector,
                SelectionKey.OP_READ | SelectionKey.OP_WRITE,
                new RequestContext()
        );

        channels.add(socketChannel);
    }

    private void read(SelectionKey key) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) key.channel();
        final RequestContext requestContext = (RequestContext) key.attachment();

        if (requestContext.count.hasRemaining()) {
            if (requestContext.count.remaining() == requestContext.count.capacity()) {
                // старт обработки 1 запроса
                requestContext.clientTime.start();
            }

            socketChannel.read(requestContext.count);

            if (!requestContext.count.hasRemaining()) {
                requestContext.count.flip();
                requestContext.nums = ByteBuffer.allocate(requestContext.count.getInt());
            }
        }

        if (requestContext.nums != null) {
            socketChannel.read(requestContext.nums);

            if (!requestContext.nums.hasRemaining()) {
                requestContext.nums.flip();

                threadPool.submit(() -> {
                    try {
                        prepareAnswer(requestContext);
                    } catch (InvalidProtocolBufferException e) {
                        LOGGER.severe("Invalid parsing!");
                    }
                });

                requestContext.refreshReading();
            }
        }
    }

    private void write(SelectionKey key) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) key.channel();
        final RequestContext requestContext = (RequestContext) key.attachment();

        if (requestContext.answer == null) {
            final Message.Answer answerPacket;

            synchronized (requestContext.lock) {
                answerPacket = requestContext.answerPacket;
                requestContext.refreshPacket();
            }

            if (answerPacket != null) {
                requestContext.answer = ByteBuffer.wrap(answerPacket.toByteArray());
                requestContext.answer.flip();
            }
        }

        if (requestContext.answer != null) {
            socketChannel.write(requestContext.answer);

            if (!requestContext.answer.hasRemaining()) {
                // конец обработки одного запроса
                requestContext.clientTime.stop();
                clientHandlingStat.add(requestContext.clientTime);

                requestContext.refreshWriting();
            }
        }
    }

    private static class RequestContext {
        private final TimeInterval clientTime;
        private final ByteBuffer count;
        private final Object lock;
        private ByteBuffer nums;
        private ByteBuffer answer;
        private Message.Answer answerPacket;

        private RequestContext() {
            clientTime = new TimeInterval();
            count = ByteBuffer.allocate(INTEGER_SIZE);
            lock = new Object();
            nums = null;
            answer = null;
            answerPacket = null;
        }

        private void refreshReading() {
            count.clear();
            nums = null;
        }

        private void refreshWriting() {
            answer = null;
        }

        private void refreshPacket() {
            answerPacket = null;
        }
    }
}

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
import java.util.LinkedList;
import java.util.List;
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
    private final List<SocketChannel> channels;

    NonBlockingTCPServer(int port) throws IOException {
        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        threadPool = Executors.newCachedThreadPool();

        channels = new LinkedList<>();
    }

    @Override
    public void start() {
        try {
            while (serverSocketChannel.isOpen()) {
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

    private void prepareAnswer(RequestContext requestContext, Message.Query query, TimeInterval requestTime)
            throws InvalidProtocolBufferException {

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

        if (requestContext.size.hasRemaining()) {
            if (requestContext.size.remaining() == requestContext.size.capacity()) {
                // старт обработки 1 запроса
                requestContext.clientTime.start();
            }

            socketChannel.read(requestContext.size);

            if (!requestContext.size.hasRemaining()) {
                requestContext.size.flip();
                requestContext.data = ByteBuffer.allocate(requestContext.size.getInt());
            }
        }

        if (requestContext.data != null) {
            socketChannel.read(requestContext.data);

            if (!requestContext.data.hasRemaining()) {
                requestContext.data.flip();

                final TimeInterval requestTime = new TimeInterval();
                requestTime.start();

                final Message.Query query = Message.Query.parseFrom(requestContext.data.array());

                threadPool.submit(() -> {
                    try {
                        prepareAnswer(requestContext, query, requestTime);
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
                requestContext.answer = ByteBuffer.allocate(INTEGER_SIZE + answerPacket.getSerializedSize());
                requestContext.answer.putInt(answerPacket.getSerializedSize());
                requestContext.answer.put(answerPacket.toByteArray());

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
        private final ByteBuffer size;
        private final Object lock;
        private ByteBuffer data;
        private ByteBuffer answer;
        private Message.Answer answerPacket;

        private RequestContext() {
            clientTime = new TimeInterval();
            size = ByteBuffer.allocate(INTEGER_SIZE);
            lock = new Object();
            data = null;
            answer = null;
            answerPacket = null;
        }

        private void refreshReading() {
            size.clear();
            data = null;
        }

        private void refreshWriting() {
            answer = null;
        }

        private void refreshPacket() {
            answerPacket = null;
        }
    }
}

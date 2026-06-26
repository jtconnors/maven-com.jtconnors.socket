package com.jtconnors.socket.test;

import com.jtconnors.socket.Constants;
import com.jtconnors.socket.DebugFlags;
import com.jtconnors.socket.GenericSocket;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Minimal client example that opens a socket connection and sends one message.
 */
public class SimpleSocketClient extends GenericSocket {

    private final CountDownLatch opened = new CountDownLatch(1);
    private final CountDownLatch closed = new CountDownLatch(1);
    private final AtomicBoolean closedReported = new AtomicBoolean();
    private final String host;

    @Override
    public void onMessage(String msg) {
        System.out.println("client received: " + msg);
    }

    @Override
    public void onClosedStatus(boolean isClosed) {
        if (isClosed && !closedReported.compareAndSet(false, true)) {
            return;
        }
        System.out.println("client socket is " + (isClosed ? "closed" : "open"));
        if (isClosed) {
            closed.countDown();
        } else {
            opened.countDown();
        }
    }

    @Override
    protected void initSocketConnection() throws SocketException {
        try {
            socketConnection = new Socket();
            socketConnection.setReuseAddress(true);
            socketConnection.connect(new InetSocketAddress(host, getPort()));
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    @Override
    protected void closeAdditionalSockets() {
    }

    private boolean awaitOpen() throws InterruptedException {
        return opened.await(5, TimeUnit.SECONDS);
    }

    private void awaitClose() throws InterruptedException {
        closed.await(5, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws InterruptedException {
        String host = args.length > 0
                ? args[0]
                : Constants.instance().DEFAULT_HOST;
        int port = args.length > 1
                ? Integer.parseInt(args[1])
                : Constants.instance().DEFAULT_PORT;
        String message = args.length > 2
                ? args[2]
                : "hello from SimpleSocketClient";

        SimpleSocketClient client = new SimpleSocketClient(host, port);
        client.connect();
        if (!client.awaitOpen()) {
            throw new IllegalStateException("Timed out waiting for socket to open");
        }
        client.sendMessage(message);
        client.shutdown();
        client.awaitClose();
    }

    public SimpleSocketClient(String host, int port) {
        super(port, DebugFlags.instance().DEBUG_NONE);
        this.host = host;
    }
}

package com.jtconnors.socket.test;

import com.jtconnors.socket.Constants;
import com.jtconnors.socket.DebugFlags;
import com.jtconnors.socket.GenericSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Minimal server example that accepts one socket connection and prints messages.
 */
public class SimpleSocketServer extends GenericSocket {

    private final CountDownLatch closed = new CountDownLatch(1);
    private final AtomicBoolean closedReported = new AtomicBoolean();
    private ServerSocket serverSocket;

    @Override
    public void onMessage(String msg) {
        System.out.println("server received: " + msg);
    }

    @Override
    public void onClosedStatus(boolean isClosed) {
        if (isClosed && !closedReported.compareAndSet(false, true)) {
            return;
        }
        System.out.println("server socket is " + (isClosed ? "closed" : "open"));
        if (isClosed) {
            closed.countDown();
        }
    }

    @Override
    protected void initSocketConnection() throws SocketException {
        try {
            serverSocket = new ServerSocket(getPort());
            serverSocket.setReuseAddress(true);
            System.out.println("server listening on port " + getPort());
            socketConnection = serverSocket.accept();
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    @Override
    protected void closeAdditionalSockets() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("failed to close server socket: " + e.getMessage());
        }
    }

    private void awaitClose() throws InterruptedException {
        closed.await();
    }

    public static void main(String[] args) throws InterruptedException {
        int port = args.length > 0
                ? Integer.parseInt(args[0])
                : Constants.instance().DEFAULT_PORT;

        SimpleSocketServer server = new SimpleSocketServer(port);
        server.connect();
        server.awaitClose();
    }

    public SimpleSocketServer(int port) {
        super(port, DebugFlags.instance().DEBUG_NONE);
    }
}

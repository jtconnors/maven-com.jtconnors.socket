
/*
 * Copyright (c) 2018, Jim Connors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of this project nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jtconnors.socket.test;

import com.jtconnors.socket.Constants;
import com.jtconnors.socket.DebugFlags;
import com.jtconnors.socket.GenericSocket;
import com.jtconnors.socket.SocketListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class demonstrates a socket client connection receiving messages from
 * its socket server.
 * <br><br>
 * It can be initiated by starting its {@code main()} method
 * ({@link com.jtconnors.socket.test.SocketClientReceiver#main}).  The
 * sever end of this connection can be initiated by the
 * {@link com.jtconnors.socket.test.MultipleSocketServerSender#main} method.
 * <br><br>
 * WARNING: This class is meant for testing outside the JavaFX framework. In
 * JavaFX we must insure that certain work must be done on the main thread, and
 * this class does no such thing.
 */
public class SocketClientReceiver extends GenericSocket
        implements SocketListener {

    private final static Logger LOGGER
            = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private volatile boolean socketClosed = true;

    public String host;
    private SocketListener socketListener;

    /**
     * Called whenever a message is read from the socket. In JavaFX, this method
     * must be run on the main thread and is accomplished by the
     * Entry.deferAction() call. Failure to do so *will* result in strange
     * errors and exceptions.
     *
     * @param line Line of text read from the socket.
     */
    @Override
    public void onMessage(final String line) {
        socketListener.onMessage(line);
    }

    /**
     * Called whenever the open/closed status of the Socket changes. In JavaFX,
     * this method must be run on the main thread and is accomplished by the
     * Entry.deferAction() call. Failure to do so will* result in strange errors
     * and exceptions.
     *
     * @param isClosed true if the socket is closed
     */
    @Override
    public void onClosedStatus(final boolean isClosed) {
        socketListener.onClosedStatus(isClosed);
        socketClosed = isClosed;
    }

    /**
     * Initialize the SocketClient up to and including issuing the accept()
     * method on its socketConnection.
     *
     * @throws java.net.SocketException is a socket error occurs
     */
    @Override
    public void initSocketConnection() throws SocketException {
        try {
            socketConnection = new Socket();
            /*
             * Allows the socket to be bound even though a previous
             * connection is in a timeout state.
             */
            socketConnection.setReuseAddress(true);
            /*
             * Create a socket connection to the server
             */
            socketConnection.connect(new InetSocketAddress(host, getPort()));
            if (debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                LOGGER.log(Level.INFO, "Connected to {0} at port {1}",
                        new Object[]{host, getPort()});
            }
        } catch (IOException e) {
            if (debugFlagIsSet(DebugFlags.instance().DEBUG_EXCEPTIONS)) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                LOGGER.log(Level.SEVERE, sw.toString());
            }
            throw new SocketException();
        }
    }

    /**
     * For SocketClient class, no additional work is required. Method is null.
     */
    @Override
    public void closeAdditionalSockets() {
    }

    public SocketClientReceiver(SocketListener socketListener) {
        this(socketListener, Constants.instance().DEFAULT_HOST,
                Constants.instance().DEFAULT_PORT,
                DebugFlags.instance().DEBUG_NONE);
    }

    public SocketClientReceiver(SocketListener socketListener,
            String host, int port) {
        this(socketListener, host, port, DebugFlags.instance().DEBUG_NONE);
    }

    public SocketClientReceiver(SocketListener socketListener,
            String host, int port, int debugFlags) {
        super(port, debugFlags);
        this.host = host;
        this.socketListener = socketListener;
    }

    /**
     * This main method will repeatedly attempt to open up a socket connection
     * to a SocketServer instance until it is successful. When a connection is
     * established, it will send a message to the SocketServer whenever the user
     * hits {@code <RETURN>}. The connection (and program) is terminated if the
     * input line is {@code 'q'} followed by {@code <Return>}
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        SocketClientReceiver socketClient;
        do {
            socketClient = new SocketClientReceiver(new SocketListener() {
                @Override
                public void onMessage(String msg) {
                    // Should never read anything
                }

                @Override
                public void onClosedStatus(boolean isClosed) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("onClosedStatus: socket is ");
                    if (isClosed) {
                        sb.append("closed.");
                    } else {
                        sb.append("open.");
                    }
                    LOGGER.log(Level.INFO, sb.toString());
                }
            },
                    Constants.instance().DEFAULT_HOST,
                    Constants.instance().DEFAULT_PORT,
                    DebugFlags.instance().DEBUG_SEND
                    | DebugFlags.instance().DEBUG_RECV
                    | DebugFlags.instance().DEBUG_STATUS
            );
            socketClient.connect();
            if (socketClient.socketClosed) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        } while (socketClient.socketClosed);

//        String line = "";
//        int messageNum = 0;
//        while (true) {
//            System.out.println("type <RETURN> to send message ('q' to exit): ");
//            InputStreamReader converter = new InputStreamReader(System.in);
//            BufferedReader in = new BufferedReader(converter);
//            try {
//                line = in.readLine();
//            } catch (IOException e) {
//            }
//            if (line.equals("q")) {
//                break;
//            } else {
//                String msg = "message " + ++messageNum;
//                socketClient.sendMessage(msg);
//            }
//        }
//        socketClient.shutdown();
    }

}


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
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class demonstrates a socket server connection receiving messages from
 * a socket client.
 * <br><br>
 * It can be initiated by starting its {@code main()} method
 * ({@link com.jtconnors.socket.test.SocketServerReceiver#main}).  The
 * client end of this connection can be initiated by the
 * {@link com.jtconnors.socket.test.SocketClientSender#main} method.
 * <br><br>
 * WARNING: This class is meant for testing outside the JavaFX framework. In
 * JavaFX we must insure that certain work must be done on the main thread, and
 * this class does no such thing.
 */
public class SocketServerReceiver extends GenericSocket
        implements SocketListener {
    
    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private SocketListener socketListener;
    private ServerSocket serverSocket;

    /**
     * Called whenever a message is read from the socket. In JavaFX, this method
     * must be run on the main thread and is accomplished by the
     * Platform.runLater() call. Failure to do so *will* result in strange
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
     * Platform.runLater() call. Failure to do so *will* result in strange
     * errors and exceptions.
     *
     * @param isClosed true if the socket is closed
     */
    @Override
    public void onClosedStatus(final boolean isClosed) {
        socketListener.onClosedStatus(isClosed);
    }

    /**
     * Initialize the SocketServer up to and including issuing the accept()
     * method on its socketConnection.
     *
     * @throws java.net.SocketException if a socket error occurs
     */
    @Override
    public void initSocketConnection() throws SocketException {
        try {
            /*
             * Create server socket
             */
            serverSocket = new ServerSocket(getPort());
            /*
             * Allows the socket to be bound even though a previous
             * connection is in a timeout state.
             */
            serverSocket.setReuseAddress(true);
            /*
             * Wait for connection
             */
            if (debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                LOGGER.info("Waiting for connection");
            }
            socketConnection = serverSocket.accept();
            if (debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                LOGGER.log(Level.INFO, "Connection received from {0}",
                        socketConnection.getInetAddress().getHostName());
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
     * For SocketServer class, additional ServerSocket instance has to be
     * closed.
     */
    @Override
    public void closeAdditionalSockets() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            LOGGER.log(Level.SEVERE, sw.toString());
        }
    }

    public SocketServerReceiver(SocketListener socketListener,
            int port, int debugFlags) {
        super(port, debugFlags);
        this.socketListener = socketListener;
    }

    public SocketServerReceiver(SocketListener socketListener) {
        this(socketListener, Constants.instance().DEFAULT_PORT,
                DebugFlags.instance().DEBUG_NONE);
    }

    public SocketServerReceiver(SocketListener socketListener, int port) {
        this(socketListener, port, DebugFlags.instance().DEBUG_NONE);
    }

    /**
     * This main method will create a SocketServer instance and wait
     * for a SocketClient instance to establish a connection.  When 
     * established, it will receive messages from the SocketClient until
     * the connection is terminated.
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        SocketListener socketListener = new SocketListener() {
            @Override
            public void onMessage(String line) {
                LOGGER.log(Level.INFO, "onMessage: {0}", line);
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
        };
        SocketServerReceiver socketServer = new SocketServerReceiver(
                socketListener,
                Constants.instance().DEFAULT_PORT,
                DebugFlags.instance().DEBUG_ALL);
        socketServer.connect();
    }
}

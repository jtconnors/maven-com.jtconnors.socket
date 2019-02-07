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

package com.jtconnors.socket;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides the framework for a ServerSocket connection with
 * multiple listeners.  When an update message is to be posted, all listeners
 * will receive the message on their socket connection.
 * <br><br>
 * This class is abstract and requires implementation of the 
 * {@link com.jtconnors.socket.SocketListener#onMessage} and
 * {@link com.jtconnors.socket.SocketListener#onClosedStatus} methods.
 */
public abstract class MultipleSocketWriter extends SocketBase
        implements SocketListener, Runnable {
    
    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    private int listenerPort;
    private ServerSocket serverSocket;
    private List<MultipleSocketWriterListener> updateListeners;
    /**
     * Reference to this instance, used to avoid "leaking this in constructor"
     * warning.
     */
    protected MultipleSocketWriter multipleSocketWriterRef;
    
    class MultipleSocketWriterListener implements SocketListener {
        
        private PrintWriter writer;
        private BufferedReader reader;
        private Socket socket;
        
        @Override
        public void onMessage(String msg) {
            multipleSocketWriterRef.onMessage(msg);        
        }
        
        @Override
        public void onClosedStatus(boolean isClosed) {
            multipleSocketWriterRef.onClosedStatus(isClosed);    
        } 
             
        /*
         * Close down the Socket infrastructure.  As per the Java Socket
         * API, once a Socket has been closed, it is not available for
         * further networking use (i.e. can't be reconnected or rebound).
         * A new Socket needs to be created.
         */
        private void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                    LOGGER.info("Connection closed");
                }
                removeListener(this);
            } catch (IOException e) {
                if (debugFlagIsSet(DebugFlags.instance().DEBUG_EXCEPTIONS)) {
                    LOGGER.severe(GenericSocket.ExceptionStackTraceAsString(e));
                }
            }
        }
        
        /*
         * Even if we don't read anything from the socket, set up a
         * ReaderThread because it will enable us to detect when a
         * socket connection has been closed.
         */
        class ReaderThread extends Thread {
            @Override
            public void run() {
                /*
                 * Read from input stream one line at a time.  The read
                 * loop will terminate when the socket connection is closed.
                 */
                try {
                    if (reader != null) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (debugFlagIsSet(
                                    DebugFlags.instance().DEBUG_RECV)) {
                                LOGGER.log(Level.INFO, "recv> {0}", line);
                            }
                            onMessage(line);
                        }
                    }
                } catch (IOException e) {
                    if (debugFlagIsSet(DebugFlags.instance().DEBUG_EXCEPTIONS)) {
                        LOGGER.info(GenericSocket.ExceptionStackTraceAsString(e));
                    }
                } finally {
                    close();
                }
            }
        }
        
        public void sendMessage(String line) {
            try {
                if (debugFlagIsSet(DebugFlags.instance().DEBUG_SEND)) {
                    LOGGER.log(Level.INFO, "send> {0}", line);
                }
                writer.println(line);
                if (writer.checkError()) {
                    multipleSocketWriterRef.removeListener(this);
                }
            } catch (Exception ex) {
                multipleSocketWriterRef.removeListener(this);
            }
        }
        
        private void setup(Socket socket) throws IOException {
            this.socket = socket;
            /* 
             * Leave check for null here.  At startup, we create a null
             * listener just so that we can call onClosedStatus(true) to
             * print out the connection status line.
             */
            if (socket != null) {
                writer = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream()));
                reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                new ReaderThread().start();
            }
        }
        
        public MultipleSocketWriterListener(Socket socket) throws IOException {
            setup(socket);
        }
    }
    
    private void addListener(SocketListener listener) {
        updateListeners.add((MultipleSocketWriterListener) listener);
        listener.onClosedStatus(false);
        if (debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                    LOGGER.log(Level.INFO, 
                            "Added listener, number of Connections: {0}",
                            updateListeners.size());
        }
    }
    
    private void removeListener(SocketListener listener) {
        updateListeners.remove((MultipleSocketWriterListener) listener);
        listener.onClosedStatus(true);
        if (debugFlagIsSet(DebugFlags.instance().DEBUG_STATUS)) {
                    LOGGER.log(Level.INFO, 
                            "Removed listener, number of Connections: {0}",
                            updateListeners.size());
        }
    }
    
    /**
     * Get the number listeners (sockets) associated with this writer
     * @return the number of listeners (sockets)
     */
    public int getNumberOfListeners() {
        return updateListeners.size();
    }
    
    /**
     * Shut down the connection. As per the Java Socket API, once a Socket
     * has been closed, it is not available for further networking use (i.e.
     * can't be reconnected or rebound). A new Socket needs to be created.
     */
    public void shutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.severe(GenericSocket.ExceptionStackTraceAsString(e));
        }
    }
    
    /**
     * Starts the connection
     */
    @Override
    public void run() {        
        try {
            new MultipleSocketWriterListener(null).onClosedStatus(true);
            serverSocket = new ServerSocket(listenerPort);
            while(true) {
                Socket acceptSocket = serverSocket.accept();
                addListener(new MultipleSocketWriterListener(acceptSocket));
            }
        } catch(IOException e) {
            shutdown();
        }     
    }
    
    /*
     * Use ExecutorService to post updates.  Each individual socket will
     * get its own thread.  This construct seems to avoid a
     * ConcurrentModificationException, which happens if the updateListener
     * list is modified while iterating over it.
     */
    private final ExecutorService executor =
            new ScheduledThreadPoolExecutor(10);
    
    /**
     * Send a message to all listening sockets
     * @param msg the message String
     */
    public void postUpdate(final String msg) {      
        final MultipleSocketWriterListener[] listeners =
                this.updateListeners.toArray(
                new MultipleSocketWriterListener[this.updateListeners.size()]);
        
        for (final MultipleSocketWriterListener listener : listeners) {
            executor.submit(() -> {
                listener.sendMessage(msg);
            });    
        }
    }
    
    private void init() {
        /*
         * Avoid "leaking this in constructor" warning by moving the
         * following assignment outside the constructor into this method.
         */
        multipleSocketWriterRef = this;    
    }
    
    /**
     * MultipleSocketWriter constructor using the following default values:
     * <ul><li>{@link com.jtconnors.socket.Constants#DEFAULT_PORT}, </li>
     * <li>{@link com.jtconnors.socket.DebugFlags#DEBUG_NONE}</li></ul>
     */
    public MultipleSocketWriter () {
        this(Constants.instance().DEFAULT_PORT,
                DebugFlags.instance().DEBUG_NONE);
    }
    
    /**
     * MultipleSocketWriter constructor using the default 
     * {@link com.jtconnors.socket.DebugFlags#DEBUG_NONE} value and
     * user-specified listener port number
     * @param listenerPort the port number to initiate connections
     */
    public MultipleSocketWriter (int listenerPort) {
        this(listenerPort, DebugFlags.instance().DEBUG_NONE);
    }
    
    /**
     * MultipleSocketWriter constructor using all user-supplied values.
     * @param listenerPort Port number of connection
     * @param debugFlags debug flags used for diagnostic information
     */
    public MultipleSocketWriter(int listenerPort, int debugFlags) {
        super(debugFlags);        
        this.listenerPort = listenerPort;
        updateListeners = new ArrayList<>();
        init();
    } 
}

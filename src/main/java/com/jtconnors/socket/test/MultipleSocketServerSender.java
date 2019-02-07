
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

import com.jtconnors.socket.MultipleSocketWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class demonstrates a server socket connection that is capable of sending
 * messages to one or more connected sockets at the same time.
 * <br><br>
 * This class's 
 * {@link com.jtconnors.socket.test.MultipleSocketServerSender#main} method
 * can be executed with one or more of
 * {@link com.jtconnors.socket.test.SocketClientReceiver#main}'s methods to
 * see how this works.
 * <br><br>
 * WARNING: This class is meant for testing outside the JavaFX framework. In
 * JavaFX we must insure that certain work must be done on the main thread, and
 * this class does no such thing.
 */
public class MultipleSocketServerSender extends MultipleSocketWriter {
    
    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    
    /**
     * Called whenever a message is read from the socket.
     * In JavaFX 2.x, this method must be run on the
     * main thread.  This is accomplished by the Platform.runLater() call.
     * Failure to do so *will* result in strange errors and exceptions.
     * @param msg Line of text read from the socket.
     */
    @Override
    public void onMessage(String msg) {
        // For this version we read nothing
    }

    /**
     * Called whenever the open/closed status of the Socket
     * changes.  In JavaFX 2.x, this method must be run on the
     * main thread.  This is accomplished by the Platform.runLater() call
     * which utilizes the {@code Runnable} interface, simplified by the
     * lambda expression used in this method.
     * Failure to do so *will* result in strange errors and exceptions.
     * @param isClosed true if the socket is closed
     */
    @Override
    public void onClosedStatus(boolean isClosed) {
        LOGGER.log(Level.INFO, 
                "Number of Connections: {0}", getNumberOfListeners());
    }
    public MultipleSocketServerSender () {
        super();
    }
    
    public MultipleSocketServerSender (int listenerPort) {
        super(listenerPort);
    }
    
    public MultipleSocketServerSender(int listenerPort, int debugFlags) {
        super(listenerPort, debugFlags);
    }
    /**
     * This main method will create a MultipleSocketServerSender instance and
     * will send user-initiated messages to all connected clients until
     * terminated.
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        MultipleSocketServerSender m = new MultipleSocketServerSender() ;
        new Thread (m).start();
        
        String line = "";
        int messageNum = 0;
        while (true) {
            System.out.println("type <RETURN> to send message ('q' to exit): ");
            InputStreamReader converter = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(converter);
            try {
                line = in.readLine();
            } catch (IOException e) {
            }
            if (line.equals("q")) {
                m.shutdown();
                System.exit(0);
            } else {
                String msg = "message " + ++messageNum;
                m.postUpdate(msg);
            }
        }
    }
}

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

/**
 * This interface defines two methods that must be implemented for any class
 * that extends {@link com.jtconnors.socket.GenericSocket},
 * {@link com.jtconnors.socket.MulticastConnection} or
 * {@link com.jtconnors.socket.MultipleSocketWriter}
 */
public interface SocketListener {
    /**
     * Consumes a message. This method is invoked by registered listeners when
     * a message is received over any class that extends
     * {@link com.jtconnors.socket.GenericSocket}, 
     * {@link com.jtconnors.socket.MulticastConnection} or
     * {@link com.jtconnors.socket.MultipleSocketWriter}
     * @param msg the message contents 
     */
    public void onMessage(String msg);
    /**
     * Consumes a socket state change.  This method is invoked by registered
     * listeners when any class that extends
     * {@link com.jtconnors.socket.GenericSocket},
     * {@link com.jtconnors.socket.MulticastConnection} or
     * {@link com.jtconnors.socket.MultipleSocketWriter}
     * either opens or closes its
     * associated socket.
     * @param isClosed a {@code boolean} indicating whether the socket is closed
     * or opened.
     */
    public void onClosedStatus(boolean isClosed);
}


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
 * Contains debug flag constants that are used for this package
 * that can be referenced externally via the
 * {@link com.jtconnors.socket.DebugFlags#instance} static
 * method.  Only primitive types should be used here to avoid ambiguity.
 * <br><br>
 * It follows the Singleton design pattern and takes advantage of the 
 * properties of the Java Virtual Machine such that initialization of the
 * class instance will be done in a thread safe manner.
 * <br><br>
 * Individual debug flags are are of type {@code int} and their values should
 * be assigned a power of two so that the {@code DebugFlags} instance can be
 * viewed effectively as a bit field.
 */
public class DebugFlags {

    private DebugFlags() {
    }

    private static class LazyHolder {
        private static final DebugFlags INSTANCE = new DebugFlags();
    }
    
    /**
     * Following the Singleton design pattern, this static method insures that
     * only one instance of this class will exist, and is done so in a thread
     * safe manner.
     * @return the instance of this {@link com.jtconnors.socket.DebugFlags}
     * class
     */
    public static DebugFlags instance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * All debug flags are turned off
     */
    public final int DEBUG_NONE = 0x0;
    /**
     * Flag to log debug information on data sent over sockets
     */
    public final int DEBUG_SEND = 0x1;
    /**
     * Flag to log debug information on data received over sockets
     */
    public final int DEBUG_RECV = 0x2;
    /**
     * Flag to log exceptions that may be generated during sending/receiving
     * of sockets
     */
    public final int DEBUG_EXCEPTIONS = 0x4;
    /**
     * Flag to log changes in socket status (for example, open/close)
     */
    public final int DEBUG_STATUS = 0x8;

    /**
     * Aggregate flag, bitwise-or of {@code DEBUG_SEND | DEBUG_RECV}
     */
    public final int DEBUG_IO = DEBUG_SEND | DEBUG_RECV;
    /**
     * Aggregate flag, bitwise-or of all debug flags
     */
    public final int DEBUG_ALL
            = DEBUG_IO | DEBUG_EXCEPTIONS | DEBUG_STATUS;

    /**
     * Get a String representation of the set debug flags
     * @param debugFlags the set of debug flags 
     * @return a string representation of the debug flags that are set.
     * <br><br>
     * Example return values:
     * <br>if {@code DebugFlags} has the {@code DEBUG_READ}, {@code DEBUG_WRITE}
     * and {@code DEBUG_STATUS} bits set: {@code "DEBUG_READ | DEBUG_WRITE | DEBUG_STATUS"}
     * <br>If no flags are set: {@code "DEBUG_NONE"}
     */
    public static String toString(int debugFlags) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        if ((debugFlags & DebugFlags.instance().DEBUG_SEND) != 0) {
            sb.append("DEBUG_SEND");
            count++;
        }
        if ((debugFlags & DebugFlags.instance().DEBUG_RECV) != 0) {
            if (count > 0) {
                sb.append(" | ");
            }
            sb.append("DEBUG_RECV");
            count++;
        }
        if ((debugFlags & DebugFlags.instance().DEBUG_EXCEPTIONS) != 0) {
            if (count > 0) {
                sb.append(" | ");
            }
            sb.append("DEBUG_EXCEPTIONS");
            count++;
        }
        if ((debugFlags & DebugFlags.instance().DEBUG_STATUS) != 0) {
            if (count > 0) {
                sb.append(" | ");
            }
            sb.append("DEBUG_STATUS");
            count++;
        }
        return (count > 0) ? sb.toString() : "DEBUG_NONE";
    }
}

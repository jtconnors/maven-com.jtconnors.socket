[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jtconnors/com.jtconnors.socket/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.jtconnors/com.jtconnors.socket)

# com.jtconnors.socket

Java socket utility classes which facilitate the creation and use of:

   - socket clients

   - socket servers

   - multicast socket servers

   - socket servers capable of handling multiple socket connections

Included in this project is the ```com.jtconnors.socket.test``` package containing
rudimentary examples demonstrating how these classes can be used.  For additional applications which utilize this package, refer to the **See Also** section

The main branch targets Java 21+ and uses virtual threads for blocking socket
readers, connection setup, and multi-socket write fan-out.

Build with Maven:

```bash
mvn test
```

Run the simple socket examples in two terminals:

```bash
java --module-path target/classes \
    -m com.jtconnors.socket/com.jtconnors.socket.test.SimpleSocketServer
```

```bash
java --module-path target/classes \
    -m com.jtconnors.socket/com.jtconnors.socket.test.SimpleSocketClient
```

Useful tags: 
   - v1.0-JDK8  - Version suitable for use with Java 8 tools and runtimes
   - v1.0-JDK9  - Version suitable for use with Java 9 tools and runtimes

### See Also

Examples of applications which uses this ```com.jtconnors.socket```:

-```SocketClientFX``` (https://github.com/jtconnors/SocketClientFX)
-```SocketServerFX``` (https://github.com/jtconnors/SocketServerFX)
-```MultiSocketServerFX``` https://github.com/jtconnors/MultiSocketServerFX

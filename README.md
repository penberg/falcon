# Falcon

Falcon is a high performance, low latency FIX engine for the JVM.  It provides
an API that enables FIX connectivity for both buy side and sell side
applications such as trading systems and order management systems.

The engine is designed to avoid heap allocations in the TX and RX paths to
avoid GC pauses that are disasterious for low-latency applications.  The engine
is packed with other optimizations such as avoiding querying the system clock
for every message and open-coding formatting and parsing functions where the
JRE allocates memory implicity.

Falcon is able to achieve 23 μs RTT on average when running a latency tester
client and server on the same x86-64 machine with two cores. This means in
practice that the FIX engine overhead is around 10 μs per message sent or
received on top of network latency, with allocation rates as small as 1
KB/second per session.

## Features

* Zero-copy, non-blocking, low-latency NIO networking
* Low heap allocation rate in the FIX engine core
* Small memory footprint for session and message data structures

## Example

An example application that sends 100000 ``NewOrderSingle`` messages looks like
this:

```java
import static java.net.StandardSocketOptions.*;
import java.nio.channels.*;
import java.net.*;
import java.nio.*;

import static falcon.fix.MessageTypes.*;
import static falcon.fix.Versions.*;
import static falcon.fix.Tags.*;
import falcon.fix.*;

public class Example {
  public static void main(String[] args) throws Exception {
    SocketChannel socket = connect("localhost", 7070);

    Session session = new Session(socket, FIX_4_2, "HERMES", "INET");

    session.updateTime();

    session.send(new Message.Builder(Logon)
        .add(new Field(EncryptMethod, "0" ))
        .add(new Field(HeartBtInt,    "30"))
        .build());

    Message newOrder =
      new Message.Builder(NewOrderSingle)
          .add(new Field(EncryptMethod, "0" ))
          .add(new Field(HeartBtInt,    "30"))
          .build();

    for (int i = 0; i < 100000; i++) {
      if ((i % 10000) == 0) {
        session.updateTime();
      }
      session.send(newOrder);
    }

    session.updateTime();

    session.send(new Message.Builder(Logout).build());

    socket.close();
  }

  private static SocketChannel connect(String host, int port) throws Exception {
    InetSocketAddress addr = new InetSocketAddress(host, port);
    SocketChannel socket = SocketChannel.open();
    socket.configureBlocking(false);
    socket.setOption(TCP_NODELAY, true);
    socket.connect(addr);
    socket.finishConnect();
    return socket;
  }
}
```

## Performance

FIX engine is measured to have 23 μs round-trip time for a FIX client sending a
``NewOrderSingle`` message and waiting for ``ExecutionReport`` on x86-64 Linux
with both client and server running on the same machine.  The numbers include
Linux TCP/IP loopback overhead which is 5 μs of the RTT.

The numbers can be reproduced by starting a FIX latency test server available
in ``libtrading``:

```
$ taskset -c 0 tools/fix/fix_server -m 1 -p 7070
```

and running Falcon latency tests against it:

```
$ time ./bin/falcon-perf-test 1000000
22.812477 seconds
43835.7 messages/second
ḿin/avg/max = 20.4/22.8/997.5 μs
```

## License

Copyright © 2013 Pekka Enberg

Falcon is distributed under the 2-clause BSD license.

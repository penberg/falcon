# Falcon

Falcon is a high performance, low latency FIX engine for the JVM. It provides
an API that enables FIX connectivity for both buy side and sell side
applications such as trading systems and order management systems.

The engine is designed to avoid heap allocations on the TX and RX paths to
avoid GC pauses that are disastrous for low-latency applications. The engine
is packed with other optimizations such as avoiding querying the system clock
for every message and open-coding formatting and parsing functions where the
JRE allocates memory implicitly.

Falcon is able to achieve 8 µs RTT on when running the latency tester client
and server on the same machine.

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

The FIX engine has been measured to have 8 µs RTT for a loopback ping-pong test
where client sends a ``NewOrderSingle`` message and waits for an
``ExecutionReport`` message to arrive. The numbers include the time spent in
Linux TCP/IP stack and the loopback device.

To reproduce the results, first download and build [Libtrading]. Then start the
FIX performance test server:

```
$ taskset -c 0 tools/fix/fix_server -m 1 -p 7070
```

Finally, run the Falcon latency tests:

```
$ ./falcon-perf-test/bin/falcon-perf-test 1000000
87693.5 messages/second
min/avg/max = 9.8/11.4/19935.3 µs
Percentiles:
  1.00%: 10.15 µs
  5.00%: 10.51 µs
 10.00%: 10.61 µs
 50.00%: 11.12 µs
 90.00%: 11.90 µs
 95.00%: 13.27 µs
 99.00%: 14.53 µs
```

  [Libtrading]: https://github.com/libtrading/libtrading

## License

Copyright © 2013-2015 Pekka Enberg and contributors

Falcon is distributed under the 2-clause BSD license.

package falcon.fix.perf;

import static java.net.StandardSocketOptions.TCP_NODELAY;
import static falcon.fix.MessageTypes.*;
import static falcon.fix.Versions.*;
import static falcon.fix.Tags.*;
import java.nio.channels.*;
import java.net.*;
import java.nio.*;

import falcon.fix.*;

public class ClientPerfTest {
  public static void main(String[] args) throws Exception {
    if (args.length != 1)  {
        System.out.printf("  usage: %s <iterations>\n", ClientPerfTest.class.getSimpleName());
        System.exit(1);
    }

    int iterations = Integer.parseInt(args[0]);

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

    long duration = 0;
    long min = Long.MAX_VALUE;
    long max = 0;

    for (int i = 0; i < iterations; i++) {
      long iterationStart = System.nanoTime();

      if ((i % 10000) == 0) {
        session.updateTime();
      }

      session.send(newOrder);

      for (;;) {
        Message msg = session.recv();
        if (msg == null) {
          break;
        }
      }

      long iterationEnd = System.nanoTime();

      long iterationTime = iterationEnd - iterationStart;

      duration += iterationTime;

      min = Math.min(min, iterationTime);
      max = Math.max(max, iterationTime);
    }

    long end = System.nanoTime();

    session.updateTime();

    session.send(new Message.Builder(Logout).build());

    socket.close();

    double avg = (double)duration / (double)iterations;
    double seconds = (double)duration / 1000000000.0;
    System.out.printf("%f seconds\n", seconds);
    System.out.printf("%.1f messages/second\n", (double)iterations/seconds);
    System.out.printf("ḿin/avg/max = %.1f/%.1f/%.1f µs\n",
      (double)min / 1000.0, (double)avg / 1000.0, (double)max / 1000.0);
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

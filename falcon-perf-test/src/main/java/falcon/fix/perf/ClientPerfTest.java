package falcon.fix.perf;

import static java.net.StandardSocketOptions.TCP_NODELAY;
import static falcon.fix.MessageTypes.*;
import static falcon.fix.Versions.*;
import org.HdrHistogram.Histogram;
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

    Histogram histogram = new Histogram(3);

    for (int i = 0; i < iterations; i++) {
      long iterationStart = System.nanoTime();

      if ((i % 10000) == 0) {
        session.updateTime();
      }

      session.send(newOrder);

      for (;;) {
        Message msg = session.recv();
        if (msg != null) {
          break;
        }
      }

      long iterationEnd = System.nanoTime();

      long iterationTime = iterationEnd - iterationStart;

      duration += iterationTime;

      histogram.recordValue(iterationTime);
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
    System.out.printf("min/avg/max = %.1f/%.1f/%.1f µs\n",
      (double)min / 1000.0, (double)avg / 1000.0, (double)max / 1000.0);
    System.out.printf("Percentiles:\n");
    System.out.printf("  1.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile(  1.00)));
    System.out.printf("  5.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile(  5.00)));
    System.out.printf(" 10.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile( 10.00)));
    System.out.printf(" 50.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile( 50.00)));
    System.out.printf(" 90.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile( 90.00)));
    System.out.printf(" 95.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile( 95.00)));
    System.out.printf(" 99.00%%: %.2f µs\n", nanosToMicros(histogram.getValueAtPercentile( 99.00)));
  }

  private static SocketChannel connect(String host, int port) throws Exception {
    InetSocketAddress addr = new InetSocketAddress(host, port);
    SocketChannel socket = SocketChannel.open();
    socket.configureBlocking(false);
    socket.setOption(TCP_NODELAY, true);
    socket.connect(addr);
    while (!socket.finishConnect());
    return socket;
  }

  private static double nanosToMicros(double nano) {
    return nano / 1000.0;
  }
}

package falcon.fix;

import static falcon.fix.MessageTypes.*;
import static falcon.fix.Versions.*;
import static falcon.fix.Tags.*;

import org.github.jamm.MemoryMeter;
import java.nio.channels.*;
import org.junit.Test;

public class MemoryMeterTest {

  private MemoryMeter meter = new MemoryMeter();

  @Test
  public void footprint() throws Exception {
    Session session = new Session(SocketChannel.open(), FIX_4_2, "HERMES", "INET");
    measure(session);

    Message msg =
      new Message.Builder(Logon)
        .add(new Field(EncryptMethod, "0" ))
        .add(new Field(HeartBtInt,    "30"))
        .build();
    measure(msg);

    Field field = new Field(EncryptMethod, "0" );
    measure(field);
  }

  private void measure(Object obj) {
    System.out.printf("Memory footprint of '%s':\n\n", obj.getClass().getName());
    System.out.printf("  Size (shallow): %d bytes\n", meter.measure(obj));
    System.out.printf("  Size (deep)   : %d bytes\n", meter.measureDeep(obj));
    System.out.printf("  Child objects : %d\n", meter.countChildren(obj));
    System.out.printf("\n");
  }
}

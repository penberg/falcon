package falcon.fix;

import static org.junit.Assert.*;
import org.junit.Test;
import java.nio.*;

public class ProtocolTest {
  @Test
  public void writeInt() throws Exception {
    assertEquals("-123", convertInt(-123));
    assertEquals(   "0", convertInt(0));
    assertEquals( "123", convertInt(123));
  }

  public String convertInt(int n) throws Exception {
    ByteBuffer buf = ByteBuffer.allocate(128);
    Protocol.writeInt(buf, n);
    buf.flip();
    byte[] bytes = new byte[buf.remaining()];
    buf.get(bytes);
    return new String(bytes, "ASCII");
  }

}

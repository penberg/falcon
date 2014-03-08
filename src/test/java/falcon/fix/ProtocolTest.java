package falcon.fix;

import static org.junit.Assert.*;
import java.nio.charset.*;
import org.junit.Test;
import java.nio.*;

public class ProtocolTest {
  @Test
  public void writeInt() throws Exception {
    assertEquals("-123", writeInt(-123));
    assertEquals(   "0", writeInt(0));
    assertEquals( "123", writeInt(123));
  }

  private static String writeInt(int n) throws Exception {
    ByteBuffer buf = ByteBuffer.allocate(128);
    Protocol.writeInt(buf, n);
    buf.flip();
    byte[] bytes = new byte[buf.remaining()];
    buf.get(bytes);
    return new String(bytes, "ASCII");
  }

  @Test
  public void parseInt() throws Exception {
    assertEquals(-123, parseInt("-123\1"));
    assertEquals(   0, parseInt("0\1"));
    assertEquals( 123, parseInt("123\1"));
  }

  private static int parseInt(String s) throws Exception {
    Charset charset = Charset.forName("UTF-8");
    CharsetEncoder encoder = charset.newEncoder();
    ByteBuffer buf = encoder.encode(CharBuffer.wrap(s));
    return Protocol.parseInt(buf, (byte)0x01);
  }
}

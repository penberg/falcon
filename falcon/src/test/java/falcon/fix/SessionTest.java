package falcon.fix;

import java.nio.ByteBuffer;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SessionTest {

  private final String strStepPart = "8=FIX\0019=32\00135=A\00149=N001\00156=T002\001";
  private ByteBuffer msgBuf = ByteBuffer.allocate(1024);
  
  @Test
  public void ParseString() throws Exception {
    byte[] msgBytes = strStepPart.getBytes();
    msgBuf.put(msgBytes);
    msgBuf.flip();    
    Protocol.match(msgBuf, 8);
    assertEquals(32, Protocol.matchInt(msgBuf, 9));
    assertEquals(35, Protocol.parseInt(msgBuf, (byte)'='));
    assertEquals("A", new String(Protocol.parseString(msgBuf, (byte)0x01).data));
    msgBuf.get();
    assertEquals(49, Protocol.parseInt(msgBuf, (byte)'='));
    assertEquals("N001", new String(Protocol.parseString(msgBuf,  (byte)0x01).data));
    msgBuf.get();
    assertEquals(56, Protocol.parseInt(msgBuf, (byte)'='));
    assertEquals("T002", new String(Protocol.parseString(msgBuf,  (byte)0x01).data));           
    msgBuf.clear();

  }
}

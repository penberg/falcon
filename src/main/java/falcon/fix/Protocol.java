package falcon.fix;

import static falcon.fix.MessageTypes.*;

import java.nio.ByteBuffer;

/**
 * On-wire protocol parsing and formatting.
 */
public class Protocol {

  /*
   * Maximum message header size in bytes.
   */
  public static final int MAX_HEADER_SIZE = 64;

  /*
   * Maximum message body size in bytes.
   */
  public static final int MAX_BODY_SIZE = 4096;

  /**
   * Format field to on-wire format.
   */
  public static void format(ByteBuffer buf, int tag, byte[] value) {
    writeInt(buf, tag);
    buf.put((byte) '=');
    buf.put(value);
    buf.put((byte) 0x01);
  }

  /**
   * Format field to on-wire format.
   */
  public static void formatInt(ByteBuffer buf, int tag, int value) {
    writeInt(buf, tag);
    buf.put((byte) '=');
    writeInt(buf, value);
    buf.put((byte) 0x01);
  }

  public static void formatString(ByteBuffer buf, int tag, String value) {
    writeInt(buf, tag);
    buf.put((byte) '=');
    for (int i = 0; i < value.length(); i++) {
      buf.put((byte) value.charAt(i));
    }
    buf.put((byte) 0x01);
  }

  public static void formatCheckSum(ByteBuffer buf, int tag, int value) {
    writeInt(buf, tag);
    buf.put((byte) '=');
    if (value < 10) {
      buf.put((byte) '0');
    }
    if (value < 100) {
      buf.put((byte) '0');
    }
    writeInt(buf, value);
    buf.put((byte) 0x01);
  }

  public static void writeInt(ByteBuffer buf, int n) {
    if (n < 0) {
      buf.put((byte) '-');
    }
    n = Math.abs(n);
    int start = buf.position();
    do {
      buf.put((byte)('0' + n % 10));
      n /= 10;
    } while (n > 0);
    int end = buf.position();
    int i = start;
    int j = end - 1;
    while (i < j) {
      byte tmp = buf.get(i);
      buf.put(i, buf.get(j));
      buf.put(j, tmp);
      i++; j--;
    }
  }

  public static void match(ByteBuffer buf, int tag) throws ParseException {
    matchTag(buf, tag);
    while (buf.get() != (byte)0x01)
      ;;
  }

  public static int matchInt(ByteBuffer buf, int tag) throws ParseException {
    matchTag(buf, tag);
    return parseInt(buf, (byte)0x01);
  }

  public static int parseInt(ByteBuffer buf, byte delimiter) {
    int sign = 1;
    if (buf.get(buf.position()) == (byte)'-') {
      buf.get();
      sign = -1;
    }
    int result = 0;
    for (;;) {
      byte ch = buf.get();
      if (ch == delimiter) {
        break;
      }
      result *= 10;
      result += (byte)ch - (byte)'0';
    }
    return sign * result;
  }

  public static ByteString parseString(ByteBuffer buf, byte delimiter) {
    int start = buf.position();
    for (;;) {
      byte ch = buf.get();
      if (ch == delimiter) {
        break;
      }
    }
    int end = buf.position() - 1;
    buf.position(start);
    return ByteString.of(buf, end-start);
  }

  public static MessageType matchMsgType(ByteBuffer buf) throws ParseException {
    matchTag(buf, Tags.MsgType);
    MessageType result = null;
    switch (buf.get()) {
    case (byte)'0': result = Heartbeat;       break;
    case (byte)'1': result = TestRequest;     break;
    case (byte)'2': result = ResendRequest;   break;
    case (byte)'3': result = Reject;          break;
    case (byte)'4': result = SequenceReset;   break;
    case (byte)'5': result = Logout;          break;
    case (byte)'8': result = ExecutionReport; break;
    case (byte)'A': result = Logon;           break;
    case (byte)'D': result = NewOrderSingle;  break;
    case (byte)0x01:
      throw new ParseFailedException("Invalid MsgType (35)");
    default:
      throw new ParseFailedException("Tag specified without a value");
    }
    if (buf.get() != (byte)0x01) {
      throw new ParseFailedException("Invalid MsgType (35)");
    }
    return result;
  }

  public static void matchTag(ByteBuffer buf, int tag) throws ParseException {
    int actual = parseInt(buf, (byte)'=');
    if (actual != tag) {
      throw new ParseFailedException("Required tag missing");
    }
  }
}

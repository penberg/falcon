package falcon.fix;

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
  public static void format(ByteBuffer buf, Tag tag, byte[] value) {
    buf.put(tag.value());
    buf.put((byte) '=');
    buf.put(value);
    buf.put((byte) 0x01);
  }

  /**
   * Format field to on-wire format.
   */
  public static void formatInt(ByteBuffer buf, Tag tag, int value) {
    buf.put(tag.value());
    buf.put((byte) '=');
    writeInt(buf, value);
    buf.put((byte) 0x01);
  }

  public static void formatString(ByteBuffer buf, Tag tag, String value) {
    buf.put(tag.value());
    buf.put((byte) '=');
    for (int i = 0; i < value.length(); i++) {
      buf.put((byte) value.charAt(i));
    }
    buf.put((byte) 0x01);
  }

  public static void formatCheckSum(ByteBuffer buf, Tag tag, int value) {
    buf.put(tag.value());
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

  public static void match(ByteBuffer buf, Tag tag) throws ParseException {
    byte[] tagBytes = tag.value();
    if (buf.remaining() < tagBytes.length) {
      throw new PartialParseException();
    }
    for (int i = 0; i < tagBytes.length; i++) {
      if (buf.get() != tagBytes[i]) {
        throw new ParseFailedException();
      }
    }
    byte ch = buf.get();
    if (ch != (byte)'=') {
      throw new ParseFailedException();
    }
    while (buf.get() != (byte)0x01)
      ;;
  }

  public static int matchInt(ByteBuffer buf, Tag tag) throws ParseException {
    byte[] tagBytes = tag.value();
    if (buf.remaining() < tagBytes.length) {
      throw new PartialParseException();
    }
    for (int i = 0; i < tagBytes.length; i++) {
      if (buf.get() != tagBytes[i]) {
        throw new ParseFailedException();
      }
    }
    byte ch = buf.get();
    if (ch != (byte)'=') {
      throw new ParseFailedException();
    }
    int result = 0;
    for (;;) {
      ch = buf.get();
      if (ch == (byte)0x01) {
        break;
      }
      result *= 10;
      result += (byte)ch - (byte)'0';
    }
    return result;
  }
}

package falcon.fix;

import java.nio.ByteBuffer;

public final class ByteString {
  final byte[] data;

  /**
   * Returns a new byte string that contains a copy of <code>len</code> bytes from <code>buf</code>.
   */
  public static ByteString of(ByteBuffer buf, int len) {
    byte[] data = new byte[len];
    buf.get(data);
    return new ByteString(data);
  }

  public ByteString(byte[] data) {
    this.data = data;
  }
}

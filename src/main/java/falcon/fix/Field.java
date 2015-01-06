package falcon.fix;

import java.nio.ByteBuffer;

/**
 * A field is composed of a tag and value pair.
 */
public class Field {

  private int tag;
  private Object value;

  public Field(int tag, Object value) {
    this.tag   = tag;
    this.value = value;
  }

  public int tag() {
    return tag;
  }

  public Object value() {
    return value;
  }
}

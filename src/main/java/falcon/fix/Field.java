package falcon.fix;

import java.nio.ByteBuffer;

/**
 * A field is composed of a tag and value pair.
 */
public class Field {

  private Tag    tag;
  private Object value;

  public Field(Tag tag, Object value) {
    this.tag   = tag;
    this.value = value;
  }

  public Tag tag() {
    return tag;
  }

  public Object value() {
    return value;
  }
}

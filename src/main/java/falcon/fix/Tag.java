package falcon.fix;

/**
 * A tag specifies the type of a field.
 */
public class Tag {

  private byte[] value;

  public Tag(int value) {
    this(Integer.toString(value).getBytes());
  }

  public Tag(byte[] value) {
    this.value = value;
  }

  public byte[] value() {
    return value;
  }
}

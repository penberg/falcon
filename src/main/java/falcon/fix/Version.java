package falcon.fix;

/**
 * A version specifies which base standard is followed in a session.
 *
 * Session version is specified in the <b>BeginString</b> tag in message
 * header. Although not supported by the standard, many dialects are a
 * combination of two or more versions. <b>BeginString</b> thus only represents
 * the base specification and message types and tags are typically specified in
 * proprietary trading venue specifications. Session level semantics usually
 * follow the specified base standard, but not always.
 */
public class Version {

  private byte[] value;

  public Version(String value) {
    this(value.getBytes());
  }

  public Version(byte[] value) {
    this.value = value;
  }

  public byte[] value() {
    return value;
  }
}

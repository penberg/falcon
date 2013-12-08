package falcon.fix;

/**
 * A message type specifies which fields a message contains.
 */
public class MessageType {

  private String value;

  public MessageType(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}

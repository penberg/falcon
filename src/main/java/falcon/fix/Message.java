package falcon.fix;

import static falcon.fix.Tags.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A message is composed of number of fields, grouped in a header, body, and a
 * trailer.
 */
public class Message {

  private List<Field>  fields;
  private MessageType  type;

  /**
   * Constructs a message of specified type and fields.
   *
   * @param type   Type of this message.
   * @param fields Fields for this message.
   *
   * The constructed message has a header and a trailer, and the specified fields.
   */
  public Message(MessageType type, List<Field> fields) {
    this.type   = type;
    this.fields = fields;
  }

  public MessageType type() {
    return type;
  }

  public List<Field> fields() {
    return fields;
  }

  /**
   * Message builder.
   */
  public static class Builder {
    private List<Field>  fields = new ArrayList<>();
    private MessageType type;

    public Builder(MessageType type) {
      this.type = type;
    }

    public Builder add(Field field) {
      fields.add(field);
      return this;
    }

    public Message build() {
      return new Message(type, fields);
    }
  }
}

package falcon.fix;

import static falcon.fix.Tags.*;
import java.nio.channels.*;
import java.text.*;
import java.util.*;
import java.nio.*;

/**
 * A session is bi-directional stream of messages between two parties where
 * message ordering is guaranteed with monotonically increasing message
 * sequence numbers.
 */
public class Session {

  private ByteBuffer headBuf = ByteBuffer.allocate(Protocol.MAX_HEADER_SIZE);
  private ByteBuffer bodyBuf = ByteBuffer.allocate(Protocol.MAX_BODY_SIZE);
  private ByteBuffer rxBuf = ByteBuffer.allocate(Protocol.MAX_HEADER_SIZE + Protocol.MAX_BODY_SIZE);

  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
  private SocketChannel socket;
  private String senderCompId;
  private String targetCompId;
  private Version version;
  private int sequence;
  private String now;

  public Session(SocketChannel socket, Version version, String senderCompId, String targetCompId) {
    this.socket       = socket;
    this.version      = version;
    this.senderCompId = senderCompId;
    this.targetCompId = targetCompId;
  }

  public void updateTime() {
    now = dateFormat.format(new Date());
  }

  public void send(Message msg) throws Exception {
    Protocol.formatString(bodyBuf, MsgType, msg.type().value());
    Protocol.formatString(bodyBuf, SenderCompID, senderCompId);
    Protocol.formatString(bodyBuf, TargetCompID, targetCompId);
    Protocol.formatInt(bodyBuf, MsgSeqNum, sequence++);
    Protocol.formatString(bodyBuf, SendingTime, now);

    List<Field> fields = msg.fields();
    for (int i = 0; i < fields.size(); i++) {
      Field field = fields.get(i);
      Object value = field.value();
      if (value instanceof String) {
        Protocol.formatString(bodyBuf, field.tag(), (String) field.value());
      } else if (value instanceof Integer) {
        Protocol.formatInt(bodyBuf, field.tag(), ((Integer) field.value()).intValue());
      } else {
        throw new IllegalStateException();
      }
    }

    Protocol.format(headBuf, BeginString, version.value());
    Protocol.formatInt(headBuf, BodyLength, bodyBuf.position());

    Protocol.formatCheckSum(bodyBuf, CheckSum, (sum(headBuf) + sum(bodyBuf)) % 256);

    headBuf.flip();
    bodyBuf.flip();

    while (headBuf.remaining() > 0) {
      socket.write(headBuf);
    }
    while (bodyBuf.remaining() > 0) {
      socket.write(bodyBuf);
    }

    headBuf.clear();
    bodyBuf.clear();
  }

  public Message recv() throws Exception {
    int count = socket.read(rxBuf);
    if (count <= 0) {
      return null;
    }
    rxBuf.flip();
    rxBuf.mark();
    List<Field> fields = new ArrayList<Field>();
    MessageType msgType = null;
    try {
      Protocol.match(rxBuf, BeginString);
      int bodyLen = Protocol.matchInt(rxBuf, BodyLength);
      int msgTypeOffset = rxBuf.position();
      msgType = Protocol.matchMsgType(rxBuf);
      int checksumOffset = msgTypeOffset + bodyLen;
      rxBuf.position(checksumOffset);
      int checksumActual = sum(rxBuf) % 256;
      int checksumExpected = Protocol.matchInt(rxBuf, CheckSum);
      if (checksumExpected != checksumActual) {
        throw new RuntimeException(String.format("Invalid checksum: expected %d, got: %d", checksumExpected, checksumActual));
      }
      int endOffset = rxBuf.position();
      rxBuf.position(msgTypeOffset);
      while (rxBuf.position() < checksumOffset) {
        int tag = Protocol.parseInt(rxBuf, (byte)'=');
        ByteString value = Protocol.parseString(rxBuf, (byte)0x01);
        fields.add(new Field(tag, value));
      }
      rxBuf.position(endOffset);
    } catch (PartialParseException e) {
      rxBuf.reset();
      return null;
    } catch (ParseFailedException e) {
      throw new RuntimeException("Garbled message", e);
    }
    rxBuf.compact();
    return new Message(msgType, fields);
  }

  private static int sum(ByteBuffer buf) {
    return sum(buf, 0, buf.position());
  }

  private static int sum(ByteBuffer buf, int start, int end) {
    int result = 0;
    for (int i = start; i < end; i++)
      result += buf.get(i);
    return result;
  }
}

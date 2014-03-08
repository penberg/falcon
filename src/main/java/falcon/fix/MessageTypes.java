package falcon.fix;

public class MessageTypes {

  public static final MessageType Heartbeat      = new MessageType("0");
  public static final MessageType TestRequest    = new MessageType("1");
  public static final MessageType ResendRequest  = new MessageType("2");
  public static final MessageType Reject         = new MessageType("3");
  public static final MessageType SequenceReset  = new MessageType("4");
  public static final MessageType Logout         = new MessageType("5");
  public static final MessageType ExecutionReport= new MessageType("8");
  public static final MessageType Logon          = new MessageType("A");
  public static final MessageType NewOrderSingle = new MessageType("D");

}

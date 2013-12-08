package falcon.fix;

public class Tags {

  public static final Tag BeginString   = new Tag(8);
  public static final Tag BodyLength    = new Tag(9);
  public static final Tag CheckSum      = new Tag(10);
  public static final Tag MsgSeqNum     = new Tag(34);
  public static final Tag MsgType       = new Tag(35);
  public static final Tag SenderCompID  = new Tag(49);
  public static final Tag SendingTime   = new Tag(52);
  public static final Tag TargetCompID  = new Tag(56);
  public static final Tag EncryptMethod = new Tag(98);
  public static final Tag HeartBtInt    = new Tag(108);
}

package websocket.messages;

public class NotificationMessage extends ServerMessage{

    private final String msg;

    public NotificationMessage(String msg){
        super(ServerMessageType.NOTIFICATION);
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "msg='" + msg + '\'' +
                '}';
    }
}

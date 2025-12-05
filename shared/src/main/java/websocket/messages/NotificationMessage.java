package websocket.messages;

public class NotificationMessage extends ServerMessage{

    private final String msg;
//    private final NotificationMessageType notificationMessageType;
//
//    public enum NotificationMessageType{
//        OBSERVER_JOIN,
//        OBSERVER_LEAVE,
//        PLAYER_JOIN,
//        PLAYER_LEAVE,
//        PLAYER_PLAY,
//        PLAYER_RESIGN,
//    }

    public NotificationMessage(String msg
//                               NotificationMessageType type
    ){
        super(ServerMessageType.NOTIFICATION);
        this.msg = msg;
//        this.notificationMessageType = type;
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

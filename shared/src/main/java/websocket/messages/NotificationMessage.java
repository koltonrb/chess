package websocket.messages;

public class NotificationMessage extends ServerMessage{

    private final String message;
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

    public NotificationMessage(String message
//                               NotificationMessageType type
    ){
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
//        this.notificationMessageType = type;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "msg='" + message + '\'' +
                '}';
    }
}

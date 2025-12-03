package websocket.messages;

public class ErrorMessage extends ServerMessage {
    String errorMessage;

    public ErrorMessage(String msg){
        super(ServerMessageType.ERROR);
        this.errorMessage = msg;
    }
}

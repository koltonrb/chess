package client;


import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;

import jakarta.websocket.*;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;

public class WebSocketFacade extends Endpoint{

    Session session;
    private final ChessClient client;

    public WebSocketFacade(String url, ChessClient myClient) throws ResponseException{
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI( url + "/ws");
            this.client = myClient;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            // set message handler
            this.session.addMessageHandler( new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    //fixme: this only handles the server messages, may need to deserialize to a different subclass What about their subtypes?
                    ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);
                    if (msg.getServerMessageType()== ServerMessage.ServerMessageType.ERROR){
                        msg = new Gson().fromJson(message, ErrorMessage.class);
                        // fixme: abandon this basic print out?
                        client.notify(msg.toString());
                    } else if (msg.getServerMessageType()== ServerMessage.ServerMessageType.LOAD_GAME) {
                        msg = new Gson().fromJson(message, LoadGameMessage.class);
                        client.notifyDrawBoard((LoadGameMessage) msg);
                    } else if (msg.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
                        NotificationMessage notificationMsg = new Gson().fromJson(message, NotificationMessage.class);
                        client.notify( notificationMsg.getMsg() );
                        // fixme: abandon this basic print out?
                    }

                }
            });
        } catch (Throwable ex) {
            throw new ResponseException(ResponseException.Code.OtherServerError, ex.getMessage());
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig){}

    public void connectToGame(String AuthToken, Integer gameID, ChessGame.TeamColor color) throws ResponseException {
        try{
            ConnectCommand action = new ConnectCommand(AuthToken, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.OtherServerError, ex.getMessage());
        }
    }

    //TODO add other UserGameCommand options here

    public void leaveGame(String authToken, Integer gameID, ChessGame.TeamColor color) throws ResponseException {
        try{
            LeaveGameCommand action = new LeaveGameCommand(authToken, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex){
            throw new ResponseException(ResponseException.Code.OtherServerError, ex.getMessage());
        }
    }

    public void resignGame(String authToken, Integer gameID, ChessGame.TeamColor color) throws ResponseException {
        try{
            ResignCommand action = new ResignCommand(authToken, gameID, color);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.OtherServerError, ex.getMessage());
        }
    }

    public void makeMove(String authToken, Integer gameID, String start, String end) throws ResponseException {
        try {
            MakeMoveCommand action = new MakeMoveCommand(authToken, gameID, start, end);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.OtherServerError, ex.getMessage());
        }
    }

}

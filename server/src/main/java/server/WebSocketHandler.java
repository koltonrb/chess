package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import exception.DataAccessException;
import exception.UnauthorizedException;
import io.javalin.websocket.*;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final DataAccess dataAccess;

    public WebSocketHandler (DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        int gameID = -1;
        Session session = ctx.session;

        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            gameID = command.getGameID();
            String username = dataAccess.getAuth(command.getAuthToken()).username();
            connections.saveSession(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, (ConnectCommand) command );
                //TODO
//                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
//                case LEAVE -> leaveGame(session, username, (LeaveGameCommand) command);
//                case RESIGN -> resign(session, username, (ResignCommand) command);
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    public void sendMessage(Session session, ServerMessage serverMessage) {
        String msg = new Gson().toJson(serverMessage);
        if (session.isOpen()) {
            try{
                session.getRemote().sendString(msg);
            } catch (IOException ex) {
                System.out.println("Failed to send message to session; " + ex.getMessage());
            }
        }
    }

    private void connect(Session session, String username, ConnectCommand command) throws DataAccessException {
        GameData game = dataAccess.getGames().get(command.getGameID());

        if (game == null){
            throw new DataAccessException("gameID does not exist");
        }


    }

}

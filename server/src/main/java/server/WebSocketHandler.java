package server;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import exception.DataAccessException;
import exception.UnauthorizedException;
import io.javalin.websocket.*;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
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
//        System.out.println("Websocket connected");
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

            GameData game = dataAccess.getGames().get(gameID);
            if (game == null){
                throw new DataAccessException("no game data saved");
            }

            connections.saveSession(gameID, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, new Gson().fromJson(ctx.message(), ConnectCommand.class), game);
                //TODO
                case MAKE_MOVE -> makeMove(session, username,  new Gson().fromJson(ctx.message(), MakeMoveCommand.class), game);
                case LEAVE -> leaveGame(session, username, new Gson().fromJson(ctx.message(), LeaveGameCommand.class));
                case RESIGN -> resignGame(session, username, new Gson().fromJson(ctx.message(), ResignCommand.class));
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session, new ErrorMessage("Error: unauthorized"));
        } catch (DataAccessException ex ) {
            sendMessage(session, new ErrorMessage("Error: gameID could not be retrived"));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            sendMessage(session, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
//        System.out.println("Websocket closed");
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

    private void connect(Session session, String username, ConnectCommand command, GameData game) throws IOException{
        // send a load game message back to the root client
        sendMessage(session, new LoadGameMessage(game));
        // broadcast a message to the other clients that root client is connected to the game
        // as either an observer or as a player
        String broadcastMessage = "";
        if (command.getColor() == null){
            // then joining as an observer only
            broadcastMessage += String.format("%s is watching the game", username);
        } else {
            broadcastMessage += String.format("%s is playing %s", username, command.getColor().toString());
        }
        connections.broadcast(command.getGameID(), session, new NotificationMessage(broadcastMessage));
    }

    private void leaveGame(Session session, String username, LeaveGameCommand command) throws IOException {
        // broadcast a message to the other clients that the root client left the game
        String broadcastMessage = "";
        broadcastMessage += username;
        if (command.getColor() == null){
            // then an observer leaves the game
            broadcastMessage += " left the game and is no longer watching";
        } else {
            broadcastMessage += " left the game and is no longer playing ";
            broadcastMessage += " %s".format(command.getColor().toString());
        }
        connections.broadcast(command.getGameID(), session, new NotificationMessage(broadcastMessage));
        connections.removeSession(command.getGameID(), session);
    }

    private void resignGame(Session session, String username, ResignCommand command) throws IOException{
        // broadcast a message to the other clients that the root client resigned the game
        String broadcastMessage = "";
        broadcastMessage += username;
        broadcastMessage += " resigned the game and is no longer playing as ";
        broadcastMessage += String.format("%s", command.getColor());
        broadcastMessage += "\nthe game is over";
        connections.broadcast(command.getGameID(), session, new NotificationMessage(broadcastMessage));
        // session should stay active until users each leave
    }

    private void makeMove(Session session, String username, MakeMoveCommand command, GameData gameData) throws IOException {
        ChessGame updatedGame = gameData.game();
        Boolean moveIsValid = Boolean.TRUE;
        Boolean gameOver = Boolean.FALSE;
        try{
            updatedGame.makeMove( command.getMove() );
            if (updatedGame.isInCheck( command.getColor().equals(ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE)){
                // see if making the move places the other team in check, then notify
                // todo send message about check status around
                // todo also somewhere in this method update if the game was won or stalemated.
            }
            // TODO ditto for stalemate, checkmate
            // NOTE: you check if the move places the current team in danger in ChessGame.makeMove()
        } catch (InvalidMoveException e) {
            sendMessage(session, new ErrorMessage("Error: invalid move! Try again."));
            moveIsValid = Boolean.FALSE;

        }
        if (moveIsValid) {
            // now actually record the move
            try {
                dataAccess.updateGame(new GameData(gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        updatedGame,
                        gameData.canUpdate()
                ));
            } catch (DataAccessException e) {
                sendMessage(session, new ErrorMessage("Error: couldn't update game"));
            }
            // send a board (and updated game) back to the root client
            sendMessage(session, new LoadGameMessage(gameData));
            // broadcast a message to the other clients that the root client made a move
            String broadcastMessage = "";
            broadcastMessage += username;
            broadcastMessage += String.format(" made a move from %s to %s", command.getStart(), command.getEnd());
            connections.broadcast(command.getGameID(), session, new NotificationMessage(broadcastMessage));
            // and send a board (and updated game) to the other clients, too.
            connections.broadcast(command.getGameID(), session, new LoadGameMessage(gameData));
        }
    }



}

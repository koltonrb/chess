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
        // check if can leave game
        GameData currentGameData = new GameData(null, null, null, null, null, null);
        try {
            currentGameData = this.dataAccess.getGames().get( command.getGameID() );
        } catch (DataAccessException e) {
            sendMessage(session, new ErrorMessage("Error: failed to record leave"));
        }
        GameData updatedGameData;
        if (username.equals(currentGameData.whiteUsername())){
           // then playing white
            updatedGameData = new GameData(currentGameData.gameID(),
                    null,
                    currentGameData.blackUsername(),
                    currentGameData.gameName(),
                    currentGameData.game(),
                    currentGameData.canUpdate());
        } else if (username.equals(currentGameData.blackUsername())) {
            // then playing black
            updatedGameData = new GameData(currentGameData.gameID(),
                    currentGameData.whiteUsername(),
                    null,
                    currentGameData.gameName(),
                    currentGameData.game(),
                    currentGameData.canUpdate());
        } else {
            // then observer
            updatedGameData = currentGameData;
        }
        try {
            this.dataAccess.updateGame( updatedGameData );
        } catch (DataAccessException e) {
            new ErrorMessage("Error: recording leave failed.");
        }

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
        Boolean canResign = Boolean.TRUE;
        // check that you can resign the game. Are you a player in the game?
        GameData gameData = new GameData(null, null, null, null, null, null); 
        try {
            gameData = this.dataAccess.getGames().get(command.getGameID());
        } catch (DataAccessException e) {
            sendMessage(session, new ErrorMessage("Error: failed to record resign"));
        }
        if (!gameData.canUpdate()){
            // then the game is already finished or someone already resigned
            sendMessage(session, new ErrorMessage("Error: you cannot resign.  The game is already over."));
            canResign = Boolean.FALSE;
        }
        if ((canResign) && !((username.equals(gameData.whiteUsername())) || (username.equals(gameData.blackUsername())))){
            sendMessage(session, new ErrorMessage("Error: you cannot resign if you are not playing"));
            canResign = Boolean.FALSE;
        }
        if (canResign) {
            // broadcast a message to the other clients that the root client resigned the game
            String broadcastMessage = "";
            broadcastMessage += username;
            broadcastMessage += " resigned the game and is no longer playing as ";
            broadcastMessage += String.format("%s", command.getColor());
            broadcastMessage += "\nthe game is over";
            // record that the game is conceded
            try {
                this.dataAccess.concludeGame(command.getGameID());
            } catch (DataAccessException e) {
                sendMessage(session, new ErrorMessage("Error: failed to record the resignation"));
            }
            connections.broadcast(command.getGameID(), null, new NotificationMessage(broadcastMessage));
            // session should stay active until users each leave
        }
    }

    private void makeMove(Session session, String username, MakeMoveCommand command, GameData gameData) throws IOException {
        ChessGame updatedGame = gameData.game();
        Boolean moveIsValid = Boolean.TRUE;
        Boolean gameOver = Boolean.FALSE;
        ChessGame.TeamColor playingColor;
        if (command.getColor() != null) {
            playingColor = command.getColor();
        } else {
            playingColor = gameData.game().getTeamTurn();
        }
        ChessGame.TeamColor opposingColor = playingColor.equals(ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        String playingUsername;
        String opposingUsername;
        if (playingColor.equals(ChessGame.TeamColor.WHITE)) {
            playingUsername = gameData.whiteUsername();
            opposingUsername = gameData.blackUsername();
        } else {
            playingUsername = gameData.blackUsername();
            opposingUsername = gameData.whiteUsername();
        }
        if (!gameData.canUpdate()){ gameOver = Boolean.TRUE;}
        // also check if it is your turn.  there is probably a better solution using authtokens stored adjacent to the usernames games table
        if ((updatedGame.getTeamTurn() != playingColor)
                || (username.equals( opposingUsername )) || !(username.equals( playingUsername )) || (gameOver)){
            sendMessage(session, new ErrorMessage("Error: you can only play on your turn"));
            moveIsValid = Boolean.FALSE;
        }
        //check that you are not playing the other team's piece
        if ((updatedGame.getBoard().getPiece( command.getMove().getStartPosition()) != null )
                && (updatedGame.getBoard().getPiece( command.getMove().getStartPosition()).getTeamColor().equals( opposingColor ))
            && (moveIsValid)){
            sendMessage(session, new ErrorMessage("Error: you can only move your own pieces"));
            moveIsValid = Boolean.FALSE;
        }
        // check that you are not in stalemate---IE that you still have at least one legal move to make.
        if (updatedGame.isInStalemate(playingColor)) {
            // I think you only need to check stalemate for the team whose turn it is
            // ends in a draw on their turn.
            String inStalemateBroadcast = String.format("%s playing %s is in stalemate! the game is a draw.",
                    playingUsername, playingColor);
            connections.broadcast(command.getGameID(), null, new NotificationMessage(inStalemateBroadcast));
            // and record that the game is over!
            gameOver = Boolean.TRUE;
            moveIsValid = Boolean.FALSE;
            try {
                this.dataAccess.concludeGame(command.getGameID());
            } catch (DataAccessException e) {
                sendMessage(session, new ErrorMessage("Error: couldn't conclude the game at stalemate."));
            }
        }
        if ((!gameOver) && (moveIsValid)) {
            try {
                // try to make the move.  Will throw an error in not a valid move.
                updatedGame.makeMove(command.getMove());
                // should I recheck for stalemate here?  IE check if I just used my last legal move?  But no.
                // Opposing move could open some options.
            } catch (InvalidMoveException e) {
                sendMessage(session, new ErrorMessage("Error: invalid move! Try again."));
                moveIsValid = Boolean.FALSE;
            }
            if (moveIsValid) {
                if (updatedGame.isInCheckmate(opposingColor)) {
                    String inCheckMateBroadcast = String.format("%s playing %s is in checkmate!  %s playing %s wins the game!",
                            opposingUsername, opposingUsername, playingUsername, playingColor);
                    connections.broadcast(command.getGameID(), null, new NotificationMessage(inCheckMateBroadcast));
                    // and record that the game is over!
                    gameOver = Boolean.TRUE;
                    try {
                        this.dataAccess.concludeGame(command.getGameID());
                    } catch (DataAccessException e) {
                        sendMessage(session, new ErrorMessage("Error: couldn't conclude the game at checkmate."));
                    }
                }
                if (!(gameOver) && (updatedGame.isInCheck(opposingColor))) {
                    // see if making the move places the other team in check, then notify
                    String inCheckBroadcast = String.format("%s playing %s is in check!", opposingUsername, opposingColor);
                    connections.broadcast(command.getGameID(), null, new NotificationMessage(inCheckBroadcast));
                }
                // now actually record the move
                GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                        gameData.gameName(), updatedGame, gameOver);
                try {
                    dataAccess.updateGame(updatedGameData);
                } catch (DataAccessException e) {
                    sendMessage(session, new ErrorMessage("Error: couldn't update game"));
                }
                // send a board (and updated game) back to the root client
//                sendMessage(session, new LoadGameMessage(updatedGameData));
                // broadcast a message to the other clients that the root client made a move
                String broadcastMessage = "";
                broadcastMessage += username;
                broadcastMessage += String.format(" made a move from %s to %s", command.getStart(), command.getEnd());
                connections.broadcast(command.getGameID(), session, new NotificationMessage(broadcastMessage));
                // and send a board (and updated game) to the other clients, too.
                connections.broadcast(command.getGameID(), null, new LoadGameMessage(updatedGameData));
                // look for checkmate BEFORE check
            }
        }
    }
}

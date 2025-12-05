package websocket.commands;

import chess.ChessGame;

public class LeaveGameCommand extends UserGameCommand{
    public ChessGame.TeamColor getColor() {
        return color;
    }

    ChessGame.TeamColor color;

    public LeaveGameCommand(String authToken, Integer gameID, ChessGame.TeamColor color){
        super(CommandType.LEAVE, authToken, gameID);
        this.color = color;
    }
}

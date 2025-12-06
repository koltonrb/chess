package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand{
    String start;
    String end;
    ChessMove move;


    ChessGame.TeamColor color;

    public MakeMoveCommand(String authToken, Integer gameID, String start, String end, ChessMove move, ChessGame.TeamColor color) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.start = start;
        this.end = end;
        this.move = move;
        this.color = color;
    }

    public String getEnd() {
        return end;
    }

    public String getStart() {
        return start;
    }

    public ChessMove getMove() {
        return move;
    }

    public ChessGame.TeamColor getColor() {
        return color;
    }
}

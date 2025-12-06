package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand{
    String start;
    String end;

    public MakeMoveCommand(String authToken, Integer gameID, String start, String end) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.start = start;
        this.end = end;
    }

    public String getEnd() {
        return end;
    }

    public String getStart() {
        return start;
    }
}

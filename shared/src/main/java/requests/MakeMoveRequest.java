package requests;

import chess.ChessMove;

public record MakeMoveRequest(Integer gameID, ChessMove move) {
}

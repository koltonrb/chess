package requests;

import chess.ChessGame;
import model.GameData;

public record ResignGameRequest(ChessGame.TeamColor playerColor, Integer gameID) {
}

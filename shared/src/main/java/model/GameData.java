package model;

import chess.ChessGame;

public record GameData(
        Integer gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game,
        Boolean canUpdate) {

    // Compact constructor — validates and sets default
    public GameData {
        if (canUpdate == null) {
            canUpdate = true;  // default value
        }
    }

    // Convenience constructor: omit canUpdate → defaults to true
    public GameData(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        this(gameID, whiteUsername, blackUsername, gameName, game, true);
    }
}

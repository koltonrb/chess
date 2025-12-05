package websocket.messages;

import chess.ChessGame;
import model.GameData;

public class LoadGameMessage extends ServerMessage{
    private GameData gameData;

    public LoadGameMessage(GameData game){
        super(ServerMessageType.LOAD_GAME);
        this.gameData = game;
    }

    public GameData getGameData() {
        return gameData;
    }

    @Override
    public String toString() {
        return "LoadGameMessage{" +
                "game=" + gameData +
                '}';
    }
}

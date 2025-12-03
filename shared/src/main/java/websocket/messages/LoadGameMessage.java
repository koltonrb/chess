package websocket.messages;

import chess.ChessGame;

public class LoadGameMessage extends ServerMessage{
    private ChessGame game;

    public LoadGameMessage(Integer gameID){
        super(ServerMessageType.LOAD_GAME);
        // try to load the game
        try{

        }
    }
}

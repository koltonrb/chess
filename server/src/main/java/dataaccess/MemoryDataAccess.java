package dataaccess;

import chess.ChessGame;
import model.AuthData;
import Requests.ClearRequest;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> authorizations = new HashMap<>();
    final private HashMap<Integer, GameData> games = new HashMap<>();
    private Integer nextGameId = 1;

    public HashMap<String, UserData> getUsers() {
        return users;
    }
    public HashMap<String, AuthData> getAuthorizations() {
        return authorizations;
    }
    public HashMap<Integer, GameData> getGames() {
        return games;
    }

    public UserData getUser(String username) {
        return users.getOrDefault(username, null);
    }

    public void createUser(UserData user){
        users.put(user.username(), user);
    }

    public void createAuth(AuthData authData ){
//        authorizations.put(authData.username(), authData);
        authorizations.put(authData.authToken(), authData);
    }

    public AuthData getAuth(String authToken){
        return authorizations.getOrDefault(authToken, null);
    }

    public void deleteAuth(AuthData authData){
        AuthData removedAuthData = authorizations.remove(authData.authToken());
    }

    public GameData createGame(String gameName){
        GameData game = new GameData(this.nextGameId,
                null,
                null,
                gameName,
                new ChessGame());
        this.games.put(this.nextGameId, game);
        this.nextGameId++;
        return game;
    }

    public ArrayList<GameData> listGames(){
        return new ArrayList<GameData>(this.games.values());
    }

    public void updateGame(GameData updatedGame ){
        this.games.remove(updatedGame.gameID());
        this.games.put(updatedGame.gameID(), updatedGame);
    }

    public void clear(ClearRequest request){
        users.clear();
        authorizations.clear();
        games.clear();
    }

}

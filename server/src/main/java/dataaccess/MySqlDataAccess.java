package dataaccess;

import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.ClearRequest;

import java.util.ArrayList;
import java.util.HashMap;

public class MySqlDataAccess implements DataAccess {
    @Override
    public HashMap<String, UserData> getUsers() {
        return null;
    }

    @Override
    public HashMap<String, AuthData> getAuthorizations() {
        return null;
    }

    @Override
    public HashMap<Integer, GameData> getGames() {
        return null;
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(AuthData authData) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {

    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {

    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        return null;
    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData game) {

    }

    @Override
    public void clear(ClearRequest request) {

    }

}

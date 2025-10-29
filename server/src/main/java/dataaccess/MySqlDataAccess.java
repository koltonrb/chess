package dataaccess;

import exception.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import requests.ClearRequest;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class MySqlDataAccess implements DataAccess {

    public void MySqlDataAccess() throws ResponseException {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                `user_id` int NOT NULL AUTO_INCREMENT,
                `username` varchar(256) NOT NULL,
                `password` varchar(256) NOT NULL,
                'email' varchar(256) NOT NULL,
                PRIMARY KEY ('user_id')
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS authorizations (
                `authToken` varchar(256) NOT NULL,
                `username` varchar(256)
            PRIMARY KEY (authToken),
            INDEX (username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS games (
                `game_id` int NOT NULL AUTO_INCREMENT,
                `whiteUsername` varchar(256),
                `blackUsername` varchar(256),
                `gameName` varchar(256) NOT NULL,
                `game` varchar(10000),
            PRIMARY KEY ('game_id'),
            INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
    //FIXME: is `game` varchar(10000) enough characters to serialize a full chessboard? it is way too much memory?

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()){
            for (String statement : createStatements){
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

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

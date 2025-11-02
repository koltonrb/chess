package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import requests.ClearRequest;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {
        configureDatabase();
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                `user_id` int NOT NULL AUTO_INCREMENT,
                `username` varchar(256) NOT NULL UNIQUE,
                `password` varchar(256) NOT NULL,
                `email` varchar(256) NOT NULL,
                PRIMARY KEY (`user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS authorizations (
                `authToken` varchar(256) NOT NULL,
                `username` varchar(256) NOT NULL,
            PRIMARY KEY (`authToken`),
            INDEX (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,

            """
            CREATE TABLE IF NOT EXISTS games (
                `game_id` int NOT NULL AUTO_INCREMENT,
                `whiteUsername` varchar(256),
                `blackUsername` varchar(256),
                `gameName` varchar(256) NOT NULL,
                `game` varchar(10000) NOT NULL,
            PRIMARY KEY (`game_id`),
            INDEX(`gameName`)
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

    private int executeUpdate(String statement, Object... params) throws SQLException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)){
                for (int i=0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i+1, p);
                    else if (param == null) ps.setNull(i+1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException | DataAccessException e) {
            throw new SQLException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    @Override
    public HashMap<String, UserData> getUsers() throws DataAccessException {
        HashMap<String, UserData> users = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users ORDER BY user_id";
            try (PreparedStatement ps = conn.prepareStatement(statement)){
                try (ResultSet rs = ps.executeQuery()){
                    while (rs.next()) {
                        UserData user = new UserData(rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"));
                        users.put(rs.getString("username"), user);
                    }
                }
            }
        return users;
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("unable to fetch users table from database", e);
        }
    }

    @Override
    public HashMap<String, AuthData> getAuthorizations() throws DataAccessException {
        HashMap<String, AuthData> authorizations = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM authorizations ORDER BY username";
            try (PreparedStatement ps = conn.prepareStatement(statement)){
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        AuthData authData = new AuthData(rs.getString("authToken"),
                                rs.getString("username"));
                        authorizations.put(authData.authToken(), authData);
                    }
                }
            }
            return authorizations;
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("unable to fetch authorizations table from database", e);
        }
    }

    @Override
    public HashMap<Integer, GameData> getGames() throws DataAccessException {
        HashMap<Integer, GameData> games = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games ORDER BY game_id";
            try (PreparedStatement ps = conn.prepareStatement(statement)){
                try (ResultSet rs = ps.executeQuery()){
                    while (rs.next()) {
                        GameData game = new GameData(rs.getInt("game_id"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                new Gson().fromJson(rs.getString("game"), ChessGame.class));
                        games.put(game.gameID(), game);
                    }
                }
            }
            return games;
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("unable to fetch games table from database", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM authorizations WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"),
                                rs.getString("username"));
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error finding authorization", e);
        }
    }

    @Override
    public void deleteAuth(AuthData authData) throws DataAccessException {
        var statement = "DELETE FROM authorizations WHERE authToken=?";
        try {
            int id = executeUpdate(statement, authData.authToken());
        } catch (SQLException e){
            throw new DataAccessException("Database error deleting authorization", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT user_id, username, password, email FROM users WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)){
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()){
                    if (rs.next()) {
                        return new UserData(rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"));
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error finding user" + username, e);
        }
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        try {
            int id = executeUpdate(statement, userData.username(), hashedPassword, userData.email());
        } catch (SQLException e) {
            throw new DataAccessException("Database error saving user " + userData.toString(), e);
        }
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO authorizations (authToken, username) VALUES (?, ?)";
        try {
            int id = executeUpdate(statement, authData.authToken(), authData.username());
        } catch (SQLException e) {
            throw new DataAccessException("Database error creating authorization ", e );
        }
    }

    @Override
    public GameData createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        try {
            String jsonGame = new Gson().toJson( new ChessGame() );
            int gameID = executeUpdate(statement, null, null, gameName, jsonGame);
            return new GameData(gameID, null, null, gameName, new ChessGame());
        } catch (SQLException e) {
            throw new DataAccessException("Database error creating new game "+gameName, e);
        }

    }

    @Override
    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games ORDER BY game_id";
            try (PreparedStatement ps = conn.prepareStatement(statement)){
                try (ResultSet rs = ps.executeQuery()){
                    while (rs.next()) {
                        GameData game = new GameData(rs.getInt("game_id"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                new Gson().fromJson(rs.getString("game"), ChessGame.class)
                        );
                        games.add(game);
                    }
                }
            }
            return games;
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("unable to fetch games table from database", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE game_id=?";
        try {
            int gameID = executeUpdate(statement, game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName(),
                    new Gson().toJson( game.game() ),
                    game.gameID());
        } catch (SQLException e){
            throw new DataAccessException("Database error updating a game", e);
        }
    }

    @Override
    public void clear(ClearRequest request) throws DataAccessException {
        final String statements[]  = {"TRUNCATE users;",
                "TRUNCATE authorizations;",
                "TRUNCATE games;"
        };

        try {
            for (String statement : statements){
                executeUpdate(statement);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing data tables", e);
        }
    }

}

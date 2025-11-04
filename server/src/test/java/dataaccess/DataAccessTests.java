package dataaccess;

import chess.ChessGame;
import exception.DataAccessException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import requests.ClearRequest;
import requests.RegisterRequest;
import results.RegisterResult;
import service.ClearService;
import service.GameService;
import service.UserService;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataAccessTests {
    private DataAccess dataAccess;
    private UserService userService;
    private UserData initialUser;

    @BeforeAll
    static void setItAllUp() throws DataAccessException{
        DatabaseManager.createDatabase();
    }

    @BeforeEach
    void setUp() throws DataAccessException {
        this.dataAccess = new MySqlDataAccess();
        this.dataAccess.clear(new ClearRequest("db"));
        String pw = "secretPassword!";
        String hashedPw = BCrypt.hashpw(pw, BCrypt.gensalt());

        this.initialUser = new UserData("Kolton",
                hashedPw,
                "koltonrb@byu.edu");
        this.dataAccess.createUser(this.initialUser);

    }

    @Test
    @DisplayName("positive getUsers")
    void getUsersPositive() throws DataAccessException {
        HashMap<String, UserData> expectedUsers = new HashMap<String, UserData>();
        expectedUsers.put("Kolton", this.initialUser);

        HashMap<String, UserData> returnedUsers = this.dataAccess.getUsers();
        for (Map.Entry<String, UserData> entry: returnedUsers.entrySet()){
            String username = entry.getKey();
            UserData user = entry.getValue();
            Assertions.assertEquals(this.initialUser.username(), username, "user not returned by username");
            Assertions.assertEquals(user.username(), this.initialUser.username(), "mismatched UserData.userName returned");
            Assertions.assertEquals(user.email(), this.initialUser.email(), "mismatched UserData.email returned");
            Assertions.assertTrue(BCrypt.checkpw("secretPassword!", this.initialUser.password()));
        }

    }

    @Test
    @DisplayName("negative getUsers")
    void getUsersNegative() throws SQLException, DataAccessException{

        // let's break the database
        // and drop the table
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DROP TABLE IF EXISTS users")) {
            ps.executeUpdate();
        }

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                ()-> this.dataAccess.getUsers(),
                "if the users table is inaccessible or missing a DataAccessException should throw");
    }

    @Test
    @DisplayName("positive getAuthorizations")
    void getAuthorizationsPositive() throws DataAccessException {
        AuthData testAuth = new AuthData("thisWillBeRandomInRealUse", "Kolton");
        this.dataAccess.createAuth(testAuth);

        HashMap<String, AuthData> expectedAuths = new HashMap<>();
        expectedAuths.put(testAuth.authToken(), testAuth);

        HashMap<String, AuthData> returnedUsers = this.dataAccess.getAuthorizations();
        for (Map.Entry<String, AuthData> entry: returnedUsers.entrySet()){
            String authToken = entry.getKey();
            AuthData authData = entry.getValue();
            Assertions.assertEquals(testAuth.authToken(), authToken, "Authorizations not returned by authToken");
            Assertions.assertEquals(testAuth.authToken(), authData.authToken(), "returned Authorization authToken doesn't match");
            Assertions.assertEquals(testAuth.username(), authData.username(), "returned AuthData username doesn't match");
        }

    }

    @Test
    @DisplayName("negative getAuthorizations")
    void getAuthorizationsNegative() throws SQLException, DataAccessException{
        // let's break the database
        // and drop the table
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DROP TABLE IF EXISTS authorizations")) {
            ps.executeUpdate();
        }

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                ()-> this.dataAccess.getAuthorizations(),
                "if the authorizations table is inaccessible or missing a DataAccessException should throw");

    }

    @Test
    @DisplayName("positive getGames")
    void getGamesPositive() throws DataAccessException {
        String gameName = "Kolton first game!";
        GameData temp = this.dataAccess.createGame(gameName);
        GameData expectedGame = new GameData(1, null, null, gameName, new ChessGame());

        HashMap<Integer, GameData> expectedGames = new HashMap<>();
        expectedGames.put(expectedGame.gameID(), expectedGame);

        HashMap<Integer, GameData> returnedGames = this.dataAccess.getGames();

        for (Map.Entry<Integer, GameData> entry : returnedGames.entrySet()){
            Integer gameID = entry.getKey();
            GameData game = entry.getValue();
            Assertions.assertTrue(gameID > 0, "gameID must be positive");
            Assertions.assertEquals(expectedGame, game, "recorded GameData object not equal to the expected game value");
        }
    }

    @Test
    @DisplayName("negative getGames")
    void getGamesNegative() throws SQLException, DataAccessException{

        // let's break the database
        // and drop the table
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DROP TABLE IF EXISTS games")) {
            ps.executeUpdate();
        }

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                ()-> this.dataAccess.getGames(),
                "if the games table is inaccessible or missing a DataAccessException should throw");

    }

    @Test
    @DisplayName("positive getAuth")
    void getAuthPositive() throws DataAccessException {
        AuthData testAuth = new AuthData("thisWillBeRandomInRealUse", "Kolton");
        this.dataAccess.createAuth(testAuth);

        AuthData recordedAuth = this.dataAccess.getAuth(testAuth.authToken());
        Assertions.assertEquals(testAuth, recordedAuth, "recorded AuthData doesn't match supplied AuthData");
    }

    @Test
    @DisplayName("negative getAuth")
    void getAuthNegative(){
        Assertions.assertThrows(DataAccessException.class,
                ()->this.dataAccess.getAuth(null),
                "AuthTokens cannot be null");
    }

    @Test
    @DisplayName("positive deleteAuth")
    void deleteAuthPositive() throws DataAccessException {
        AuthData testAuth = new AuthData("thisWillBeRandomInRealUse", "Kolton");
        this.dataAccess.createAuth(testAuth);

        HashMap<String, AuthData> authsBeforeDelete = this.dataAccess.getAuthorizations();

        this.dataAccess.deleteAuth(testAuth);

        HashMap<String, AuthData> authsAfterDelete = this.dataAccess.getAuthorizations();
        Assertions.assertTrue(authsBeforeDelete.containsKey(testAuth.authToken()), "AuthData was not recorded correctly in db");
        Assertions.assertFalse(authsAfterDelete.containsKey(testAuth.authToken()), "AuthData was not removed properly from db");

    }

    @Test
    @DisplayName("negative deleteAuth")
    void deleteAuthNegative(){
        Assertions.assertThrows(DataAccessException.class,
                ()->this.dataAccess.deleteAuth(null),
                "AuthTokens cannot be null. A DataAccessException should be thrown if an improper delete call is made");
    }

    @Test
    @DisplayName("positive getUser")
    void getUserPositive() throws DataAccessException {
        UserData resultingUser = dataAccess.getUser("Kolton");
        Assertions.assertEquals(resultingUser.username(), this.initialUser.username(), "mismatched UserData.userName returned");
        Assertions.assertEquals(resultingUser.email(), this.initialUser.email(), "mismatched UserData.email returned");
        Assertions.assertTrue(BCrypt.checkpw("secretPassword!", this.initialUser.password()));

    }

    @Test
    @DisplayName("negative getUser")
    void getUserNegative() throws DataAccessException {
        UserData resultingUser = dataAccess.getUser("NotRegisteredName");
        Assertions.assertNull(resultingUser, "should have returned null if username not registered");
    }

    @Test
    @DisplayName("positive createUser")
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("Spongebob", "pinneappleUndertheSea", "yellow&Porous@bbmail.com");
        dataAccess.createUser(user);
        UserData recordedUser = dataAccess.getUser(user.username());
        Assertions.assertEquals(user.username(), recordedUser.username(), "mismatched username in request and db");
        Assertions.assertEquals(user.email(), recordedUser.email(), "mismatched email in request and db");
        Assertions.assertTrue(BCrypt.checkpw(user.password(), recordedUser.password()));
    }

    @Test
    @DisplayName("negative createUser")
    void createUserNegative(){
        UserData user = new UserData("Kolton", "password", "yellow&Porous@bbmail.com");
        DataAccessException myException = Assertions.assertThrows(DataAccessException.class,
                () -> dataAccess.createUser(user),
                "user creation requires unique username, but a repeated username failed to throw an error");
         }

    @Test
    @DisplayName("positive createAuth")
    void createAuthPositive() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, this.initialUser.username());
        this.dataAccess.createAuth(authData);
        AuthData recordedAuth = dataAccess.getAuth(authToken);
        Assertions.assertEquals(authData, recordedAuth, "AuthData not the same in db");
    }

    @Test
    @DisplayName("negative createAuth")
    void createAuthNegative() throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, this.initialUser.username());
        this.dataAccess.createAuth(authData);

        AuthData badAuthData = new AuthData(authToken, "Spongebob");
        DataAccessException myException = Assertions.assertThrows(DataAccessException.class,
                () -> this.dataAccess.createAuth(badAuthData),
                "authTokens must be unique");
    }

    @Test
    @DisplayName("positive createGame")
    void createGamePositive() throws DataAccessException {
        String gameName = "Kolton first game!";
        GameData game = this.dataAccess.createGame(gameName);
        GameData expectedGame = new GameData(1, null, null, gameName, new ChessGame());
        Assertions.assertEquals(expectedGame, game, "game not right at creation!");
    }

    @Test
    @DisplayName("negative createGame")
    void createGameNegative(){
        String gameName = null;
        DataAccessException myException = Assertions.assertThrows(DataAccessException.class,
                ()->this.dataAccess.createGame(gameName),
                "gameName cannot be null");
    }

    @Test
    @DisplayName("positive listGames")
    void listGamesPositive() throws DataAccessException {
        String gameName = "Kolton first game!";
        GameData game = this.dataAccess.createGame(gameName);
        GameData expectedGame = new GameData(1, null, null, gameName, new ChessGame());
        ArrayList<GameData> expectedResult = new ArrayList<>();
        expectedResult.add(expectedGame);

        ArrayList<GameData> result = this.dataAccess.listGames();
        Assertions.assertEquals(expectedResult, result, "not returning the correct list of games from db");
    }

    @Test
    @DisplayName("negative listGames")
    void listGamesNegative() throws SQLException, DataAccessException {
        // let's break the database
        // and drop the table
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DROP TABLE IF EXISTS games")) {
            ps.executeUpdate();
        }

        DataAccessException exception = Assertions.assertThrows(DataAccessException.class,
                ()-> this.dataAccess.listGames(),
                "if the games table is inaccessible or missing a DataAccessException should throw");

    }

    @Test
    @DisplayName("positive updateGame")
    void updateGamePositive() throws DataAccessException {
        String gameName = "Kolton first game!";
        GameData game = this.dataAccess.createGame(gameName);
        GameData expectedGame = new GameData(1, "Kolton", null, gameName, new ChessGame());
        this.dataAccess.updateGame( expectedGame );
        GameData recordedGame = this.dataAccess.getGames().get(1);
        Assertions.assertEquals(expectedGame, recordedGame, "game update not recorded correctly!");
    }

    @Test
    @DisplayName("negative updateGame")
    void updateGameNegative() throws DataAccessException {
        String gameName = "Kolton first game!";
        GameData game = this.dataAccess.createGame(gameName);
        GameData badGame = new GameData(1, "SpongeBob", null, gameName, null);
        DataAccessException myException = Assertions.assertThrows(DataAccessException.class,
                () -> this.dataAccess.updateGame(badGame),
                "ChessGame cannot be null! A DataAccessError should be thrown");
    }

    @Test
    @DisplayName("positive clear")
    void clearPositive() throws DataAccessException {
        ClearRequest request = new ClearRequest("db");

        HashMap<String, AuthData> expectedAuths = new HashMap<String, AuthData>();
        HashMap<Integer, GameData> expectedGames = new HashMap<Integer, GameData>();
        HashMap<String, UserData> expectedUsers = new HashMap<String, UserData>();
        // populate the game table too!
        String gameName = "Kolton first game!";
        GameData game = this.dataAccess.createGame(gameName);
        GameData expectedGame = new GameData(1, "Kolton", null, gameName, new ChessGame());
        this.dataAccess.updateGame( expectedGame );

        this.dataAccess.clear( request );

        HashMap<String, AuthData> recordedAuths = this.dataAccess.getAuthorizations();
        HashMap<Integer, GameData> recordedGames = this.dataAccess.getGames();
        HashMap<String, UserData> recordedUsers = this.dataAccess.getUsers();

        Assertions.assertEquals(expectedAuths, recordedAuths, "authorizations table not truncated");
        Assertions.assertEquals(expectedGames, recordedGames, "games table not truncated");
        Assertions.assertEquals(expectedUsers, recordedUsers, "users table not truncated");

    }

}

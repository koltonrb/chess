package client;

import exception.ResponseException;
import model.GameData;
import org.junit.jupiter.api.*;
import requests.*;
import results.JoinGameResult;
import results.LoginResult;
import results.LogoutResult;
import server.Server;

import java.util.ArrayList;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port); 
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDB() throws ResponseException {
        facade.clearDB( new ClearRequest("db"));
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("registerPositive")
    void registerPositive() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        Assertions.assertTrue(authData.authToken().length() > 10);
    }

    @Test
    @DisplayName("registerNegative")
    void registerNegative() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        ResponseException myException = Assertions.assertThrows(ResponseException.class,
                () -> facade.registerUser(new RegisterRequest("player1", "secret", "p1@yahoo.com")));
    }

    @Test
    @DisplayName("positiveLogin")
    void positiveLogin() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        var result = facade.loginUser(new LoginRequest("player1", "password"));
        Assertions.assertEquals("player1", result.username());
        Assertions.assertTrue(result.authToken().length() > 10);
    }

    @Test
    @DisplayName("negativeLogin")
    void negativeLogin() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        ResponseException myException = Assertions.assertThrows(ResponseException.class,
                () -> facade.loginUser(new LoginRequest("player1", "notTHEpassword")));

    }

    @Test
    @DisplayName("positiveLogout")
    void positiveLogout() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        facade.setAuthToken(authData.authToken());
        var loginResult = facade.loginUser(new LoginRequest("player1", "password"));
        var logoutResult = facade.logoutUser(new LogoutRequest());
        Assertions.assertNotNull(logoutResult, "Should return LogoutResult, not null");
        Assertions.assertInstanceOf(LogoutResult.class, logoutResult, "Should return LogoutResult object");
    }

    @Test
    @DisplayName("negativeLogout")
    void negativeLogout() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        // note not setting the authtoken
        var loginResult = facade.loginUser(new LoginRequest("player1", "password"));
        ResponseException myException = Assertions.assertThrows(ResponseException.class,
                () -> facade.logoutUser(new LogoutRequest()), "should fail to logout if no authtoken provided");
    }

    @Test
    @DisplayName("positiveCreateGame")
    void positiveCreateGame() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        facade.setAuthToken(authData.authToken());
        var loginResult = facade.loginUser(new LoginRequest("player1", "password"));
        var createGameResult = facade.createGame(new CreateGameRequest("game1"));
        Assertions.assertTrue(createGameResult.gameID() > 0, "created games should report their gameID number");
    }

    @Test
    @DisplayName("negativeCreateGame")
    void negativeCreateGame() throws ResponseException{
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        ResponseException myException = Assertions.assertThrows(ResponseException.class,
                ()->facade.createGame(new CreateGameRequest("game1")), "authToken needed to create a game");
    }

    @Test
    @DisplayName("positiveCreateGame")
    void positiveListGame() throws ResponseException {
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        facade.setAuthToken(authData.authToken());
        var loginResult = facade.loginUser(new LoginRequest("player1", "password"));
        var listGameResult = facade.listGames( new ListGamesRequest() );
        Assertions.assertEquals(new ArrayList<GameData>(), listGameResult.games(), "list should be empty when no games created");
    }

    @Test
    @DisplayName("negativeCreateGame")
    void negativeListGames() throws ResponseException{
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        ResponseException myException = Assertions.assertThrows(ResponseException.class,
                ()-> facade.listGames( new ListGamesRequest()), "should only be able to list games if logged in and authorized");

    }

    @Test
    @DisplayName("positiveJoinGame")
    void positveJoinGame() throws ResponseException{
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        facade.setAuthToken(authData.authToken());
        var createGameResult = facade.createGame(new CreateGameRequest("game1"));
        var joinGameResult = facade.joinGame(new JoinGameRequest("WHITE", 1));
        Assertions.assertEquals(new JoinGameResult(), joinGameResult, "problem joining game");
    }

    @Test
    @DisplayName("negativeJoinGame")
    void negativeJoinGame() throws ResponseException{
        var authData = facade.registerUser(new RegisterRequest("player1", "password", "p1@email.com"));
        facade.setAuthToken(authData.authToken());
        var createGameResult = facade.createGame(new CreateGameRequest("game1"));
        ResponseException myException = Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(new JoinGameRequest("WHITE", 2)),
                "you should throw an error when the user tries to join a game that does not exist");

    }

}

package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private DataAccess dataAccess;
    private UserService userService;
    private GameService gameService;
    private ClearService clearService;
    private RegisterResult initialRegisterResult;

    @BeforeEach
    void setUpService() throws DataAccessException {
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.clearService = new ClearService(dataAccess);

        RegisterRequest request = new RegisterRequest("Kolton",
                "secretPassword!",
                "koltonrb@byu.edu");

        this.initialRegisterResult = this.userService.register( request );
    }

    @Test
    @DisplayName("positive register")
    void positiveRegister() throws DataAccessException {

        Assertions.assertNotNull(this.initialRegisterResult, "Returned user is not null, should not be null");
        Assertions.assertInstanceOf(RegisterResult.class, this.initialRegisterResult, "Service returns RegisterRequest object");
        Assertions.assertNotNull(this.initialRegisterResult.authToken(), "Returned AuthToken is not null, should not be null");
        Assertions.assertTrue(this.initialRegisterResult.authToken().length() == 36,
                "The authtoken is a string with 36 characters");

        // now did it also add the user to memory?
        UserData user = this.dataAccess.getUser("Kolton");
        Assertions.assertEquals("Kolton", user.username(), "username stored correctly");
        Assertions.assertEquals("secretPassword!", user.password(), "password stored correctly");
        Assertions.assertEquals("koltonrb@byu.edu", user.email(), "email stored correctly") ;
    }

    @Test
    @DisplayName("negative register")
    void negativeRegister() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Kolton",
                "secretPassword!",
                "koltonrb@byu.edu");

        AlreadyTakenException myException = Assertions.assertThrows(AlreadyTakenException.class,
                () -> userService.register( request ),
                "matching users should not be able to register and should throw an error");
    }

    @Test
    @DisplayName("positive clear")
    void positiveClear() throws DataAccessException {
        HashMap<String, UserData> emptyUsers = new HashMap<>();
        HashMap<String, AuthData> emptyAuth = new HashMap<>();
        ArrayList<GameData> emptyGames = new ArrayList<>();

        ClearRequest clearRequest = new ClearRequest("");

        Assertions.assertNotEquals(emptyUsers, this.dataAccess.getUsers(), "User not registered" );
        Assertions.assertNotEquals(emptyAuth, this.dataAccess.getAuthorizations(),
                "AuthData not saved correctly");

        ClearResult clearResult = this.clearService.clear( clearRequest );

        Assertions.assertEquals(emptyUsers, this.dataAccess.getUsers(), "Users not emptied" );
        Assertions.assertEquals(emptyGames, this.dataAccess.listGames(), "games list not emptied");
        Assertions.assertEquals(emptyAuth, this.dataAccess.getAuthorizations(),
                "AuthData not emptied");

    }

    @Test
    @DisplayName("positive login")
    void positiveLogin() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("Kolton", "secretPassword!");
        LoginResult loginResult = this.userService.login(loginRequest);
        Assertions.assertNotNull(loginResult, "login result should not be null");
        Assertions.assertNotNull(loginResult.authToken(), "authtoken should not be null");
        Assertions.assertEquals(loginRequest.username(), loginResult.username(), "username changed");
    }

    @Test
    @DisplayName("negative login")
    void negativeLogin() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("Kolton", "notMyPassword");
        UnauthorizedException myException = Assertions.assertThrows(UnauthorizedException.class,
                () -> this.userService.login( loginRequest ),
                "should throw an UnauthorizedException mismatched username/password combinations");
    }

    @Test
    @DisplayName("positive logout")
    void positiveLogout() throws DataAccessException{
        LoginRequest loginRequest = new LoginRequest("Kolton", "secretPassword!");
        LoginResult loginResult = this.userService.login(loginRequest);
        LogoutRequest logoutRequest = new LogoutRequest();
        LogoutResult logoutResult = this.userService.logout(logoutRequest, loginResult.authToken());
        Assertions.assertNotNull(logoutResult, "Should return LogoutResult, not null");
        Assertions.assertInstanceOf(LogoutResult.class, logoutResult, "Should return LogoutResult object");
    }

    @Test
    @DisplayName("negative logout")
    void negativeLogout() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("Kolton", "secretPassword!");
        LoginResult loginResult = this.userService.login(loginRequest);
        LogoutRequest logoutRequest = new LogoutRequest();
        UnauthorizedException myException = Assertions.assertThrows(UnauthorizedException.class,
                () -> this.userService.logout(logoutRequest, "NOTMYAUTHTOKEN"));
    }

    @Test
    @DisplayName("positive createGame")
    void positiveCreateGame() throws DataAccessException {
        CreateGameRequest createGameRequest = new CreateGameRequest("Checkers");
        CreateGameResult createGameResult = this.gameService.createGame(createGameRequest, this.initialRegisterResult.authToken());
        Assertions.assertNotNull(createGameResult, "should return CreateGameResult, not null");
        Assertions.assertEquals(createGameResult, new CreateGameResult(1), "unexpected gameId value");
    }

    @Test
    @DisplayName("negative createGame")
    void negativeCreateGame() throws DataAccessException{
        CreateGameRequest createGameRequest = new CreateGameRequest("Checkers");
        UnauthorizedException myException = Assertions.assertThrows(UnauthorizedException.class,
                () -> this.gameService.createGame(createGameRequest, "notAnAuthorization"));

    }

    @Test
    @DisplayName("positive listGames")
    void positiveListGames() throws DataAccessException{
        ArrayList<GameData>  expected_game = new ArrayList<>();
        expected_game.add(new GameData(1,
                null,
                null,
                "BattleFrontII",
                new ChessGame()));
        ListGamesResult expected_result = new ListGamesResult(expected_game);

        CreateGameRequest createGameRequest = new CreateGameRequest("BattleFrontII");
        CreateGameResult createGameResult = this.gameService.createGame(createGameRequest,
                this.initialRegisterResult.authToken());
        ListGamesRequest listGamesRequest = new ListGamesRequest();
        ListGamesResult listGamesResult = this.gameService.listGames(listGamesRequest,
                this.initialRegisterResult.authToken());
        Assertions.assertEquals(expected_result, listGamesResult, "un expected ListGamesResult");
    }

    @Test
    @DisplayName("negative listGames")
    void negativeListGames() throws DataAccessException{
        ListGamesRequest listGamesRequest = new ListGamesRequest();
        UnauthorizedException myException = Assertions.assertThrows(UnauthorizedException.class,
                () -> this.gameService.listGames(listGamesRequest, "notAnAuthorization"));
    }

    @Test
    @DisplayName("positive joinGame")
    void positiveJoinGame() throws DataAccessException{

        CreateGameRequest createGameRequest = new CreateGameRequest("BattleFrontII");
        CreateGameResult createGameResult = this.gameService.createGame(createGameRequest,
                this.initialRegisterResult.authToken());

        JoinGameRequest joinGameRequest = new JoinGameRequest("BLACK", 1);
        JoinGameResult joinGameResult = this.gameService.joinGame(joinGameRequest, this.initialRegisterResult.authToken());
        GameData expectedGame = new GameData(1,
                null,
                this.initialRegisterResult.username(),
                "BattleFrontII",
                new ChessGame());

        GameData actualGame = this.dataAccess.getGames().get(1);

        Assertions.assertEquals(expectedGame, actualGame, "player not correctly added to game");
    }

    @Test
    @DisplayName("negative joinGame")
    void negativeJoinGame() throws DataAccessException{
        CreateGameRequest createGameRequest = new CreateGameRequest("BattleFrontII");
        CreateGameResult createGameResult = this.gameService.createGame(createGameRequest,
                this.initialRegisterResult.authToken());

        JoinGameRequest joinGameRequest = new JoinGameRequest("BLACK", 1);
        JoinGameResult joinGameResult = this.gameService.joinGame(joinGameRequest, this.initialRegisterResult.authToken());

        AlreadyTakenException myException = Assertions.assertThrows(AlreadyTakenException.class,
                () -> this.gameService.joinGame(joinGameRequest, this.initialRegisterResult.authToken()));

    }
}
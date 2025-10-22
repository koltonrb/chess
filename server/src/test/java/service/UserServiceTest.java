package service;

import dataaccess.*;
import model.*;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private DataAccess dataAccess;
    private UserService userService;
    private ClearService clearService;
    private RegisterResult initialRegisterResult;

    @BeforeEach
    void setUpService() throws DataAccessException {
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);
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

        ClearRequest clearRequest = new ClearRequest("");

        Assertions.assertNotEquals(emptyUsers, this.dataAccess.getUsers(), "User not registered" );
        Assertions.assertNotEquals(emptyAuth, this.dataAccess.getAuthorizations(),
                "AuthData not saved correctly");

        ClearResult clearResult = this.clearService.clear( clearRequest );

        Assertions.assertEquals(emptyUsers, this.dataAccess.getUsers(), "Users not emptied" );
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
}
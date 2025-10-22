package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
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

    @BeforeEach
    void setUpService(){
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);
        this.clearService = new ClearService(dataAccess);
    }

    @Test
    @DisplayName("positive register")
    void positiveRegister() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("Kolton",
                "secretPassword!",
                "koltonrb@byu.edu");

        RegisterResult result = this.userService.register( request );

        Assertions.assertNotNull(result, "Returned user is not null, should not be null");
        Assertions.assertInstanceOf(RegisterResult.class, result, "Service returns RegisterRequest object");
        Assertions.assertNotNull(result.authToken(), "Returned AuthToken is not null, should not be null");
        Assertions.assertTrue(result.authToken().length() == 36,
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

        RegisterResult result = this.userService.register( request );
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

        RegisterRequest registerRequest = new RegisterRequest("Kolton",
                "secretPassword!",
                "koltonrb@byu.edu");

        RegisterResult result = this.userService.register( registerRequest );

        Assertions.assertNotEquals(emptyUsers, this.dataAccess.getUsers(), "User not registered" );
        Assertions.assertNotEquals(emptyAuth, this.dataAccess.getAuthorizations(),
                "AuthData not saved correctly");

        ClearResult clearResult = this.clearService.clear( clearRequest );

        Assertions.assertEquals(emptyUsers, this.dataAccess.getUsers(), "Users not emptied" );
        Assertions.assertEquals(emptyAuth, this.dataAccess.getAuthorizations(),
                "AuthData not emptied");

    }
}
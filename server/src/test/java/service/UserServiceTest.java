package service;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private DataAccess dataAccess;
    private UserService userService;

    @BeforeEach
    void setUpService(){
        this.dataAccess = new MemoryDataAccess();
        this.userService = new UserService(dataAccess);
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
}
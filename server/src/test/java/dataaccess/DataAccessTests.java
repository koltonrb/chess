package dataaccess;

import exception.DataAccessException;
import model.AuthData;
import model.UserData;
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
//        RegisterRequest request2 = new RegisterRequest("Patrick",
//                "StarPassword",
//                "underMyRock@bbmail.com");

    }

    @Test
    @DisplayName("positive getUsers")
    void getUsersPositive(){
    }

    @Test
    @DisplayName("negative getUsers")
    void getUsersNegative(){
    }

    @Test
    @DisplayName("positive getAuthorizations")
    void getAuthorizationsPositive(){
    }

    @Test
    @DisplayName("negative getAuthorizations")
    void getAuthorizationsNegative(){
    }

    @Test
    @DisplayName("positive getGames")
    void getGamesPositive(){
    }

    @Test
    @DisplayName("negative getGames")
    void getGamesNegative(){
    }

    @Test
    @DisplayName("positive getAuth")
    void getAuthPositive(){
    }

    @Test
    @DisplayName("negative getAuth")
    void getAuthNegative(){
    }

    @Test
    @DisplayName("positive deleteAuth")
    void deleteAuthPositive(){
    }

    @Test
    @DisplayName("negative deleteAuth")
    void deleteAuthNegative(){
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
    void createGamePositive(){
    }

    @Test
    @DisplayName("negative createGame")
    void createGameNegative(){
    }

    @Test
    @DisplayName("positive listGames")
    void listGamesPositive(){
    }

    @Test
    @DisplayName("negative listGames")
    void listGamesNegative(){
    }

    @Test
    @DisplayName("positive updateGame")
    void updateGamePositive(){
    }

    @Test
    @DisplayName("negative updateGame")
    void updateGameNegative(){
    }

    @Test
    @DisplayName("positive clear")
    void clearPositive(){
    }

}

package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

        public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
            // TODO this should throw an exception?  Or the data access method itself?

            UserData userData = dataAccess.getUser(registerRequest.username());
            if (userData != null) {
                throw new DataAccessException("Username already exists");
            }
            UserData userToCreate = new UserData(registerRequest.username(),
                    registerRequest.password(),
                    registerRequest.email());

            dataAccess.createUser( userToCreate );

            // now let's make the user some authorization data
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, userToCreate.username());
            dataAccess.createAuth( authData );

            // now get ready to return
            return new RegisterResult(authData.authToken(), authData.username());
        }
//        public LoginResult login(LoginRequest loginRequest) {}
//        public void logout(LogoutRequest logoutRequest) {}
}


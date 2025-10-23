package service;

import Requests.LoginRequest;
import Requests.LogoutRequest;
import Requests.RegisterRequest;
import Results.LoginResult;
import Results.LogoutResult;
import Results.RegisterResult;
import dataaccess.*;
import model.*;

import java.util.UUID;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws BadRequestException, AlreadyTakenException,
            DataAccessException {
        if ((registerRequest.username() == null)
                || (registerRequest.password() == null)
                || (registerRequest.email() == null)){
            throw new BadRequestException("Bad Request");
        }
        UserData userData = dataAccess.getUser(registerRequest.username());
        if (userData != null) {
            throw new AlreadyTakenException("Username already exists");
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
    public LoginResult login(LoginRequest loginRequest) throws BadRequestException, DataAccessException {

        if ((loginRequest.username() == null) || (loginRequest.password() == null)){
            throw new BadRequestException("Bad Request");
        }
        UserData user = dataAccess.getUser(loginRequest.username() );

        if ((user == null) || (!loginRequest.password().equals(user.password())) ){
            throw new UnauthorizedException("unauthorized");
        }

        // now let's make the user some authorization data
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        dataAccess.createAuth( authData );

        return new LoginResult(authData.username(), authData.authToken());

    }

    public LogoutResult logout(LogoutRequest logoutRequest, String authToken) throws DataAccessException {
        // check if user is currently authorized
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null){
            throw new UnauthorizedException("unauthorized");
        }
        dataAccess.deleteAuth(authData);
        return new LogoutResult();

    }


//        public void logout(LogoutRequest logoutRequest) {}
}


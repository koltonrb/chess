package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.RegisterRequest;
import model.RegisterResult;
import model.UserData;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }
        public RegisterResult register(RegisterRequest registerRequest) throws DataAccessException {
            // TODO this should throw an exception?
            UserData userData = dataAccess.getUser( registerRequest.username() );
            if (userData != null){
                throw new DataAccessException("Username already exists");
            }
            
        }
//        public LoginResult login(LoginRequest loginRequest) {}
//        public void logout(LogoutRequest logoutRequest) {}
}


package service;

import dataaccess.DataAccess;

public class UserService {

    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }
//        public RegisterResult register(RegisterRequest registerRequest) {}
//        public LoginResult login(LoginRequest loginRequest) {}
//        public void logout(LogoutRequest logoutRequest) {}
}


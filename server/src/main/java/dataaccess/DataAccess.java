package dataaccess;

import model.AuthData;
import model.ClearRequest;
import model.UserData;

import java.util.HashMap;

public interface DataAccess {
    HashMap<String, UserData> getUsers();
    public HashMap<String, AuthData> getAuthorizations();
    public AuthData getAuth(String authToken) throws DataAccessException;
    void deleteAuth(AuthData authData) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData userData) throws DataAccessException;
    void createAuth(AuthData authData) throws DataAccessException;
    void clear(ClearRequest request);
}

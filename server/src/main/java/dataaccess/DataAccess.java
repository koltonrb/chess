package dataaccess;

import model.AuthData;
import model.ClearRequest;
import model.UserData;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;
    void createUser(UserData userData) throws DataAccessException;
    void createAuth(AuthData authData) throws DataAccessException;
    void clear(ClearRequest request);
}

package dataaccess;

import model.RegisterRequest;
import model.UserData;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;
    UserData createUser(RegisterRequest);
}

package dataaccess;

import model.UserData;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;
}

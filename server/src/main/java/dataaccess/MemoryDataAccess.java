package dataaccess;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> authorizations = new HashMap<>();

    public UserData getUser(String username) {
        return users.getOrDefault(username, null);
    }

    public void createUser(UserData user){
        users.put(user.username(), user);
    }

    public void createAuth(AuthData authData ){
        authorizations.put(authData.username(), authData);
    }
}

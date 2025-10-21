package dataaccess;

import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private int nextId = 1;
    final private HashMap<String, UserData> users = new HashMap<>();

    public UserData getUser(String username){
        if (users.containsKey(username)) {
            return users.get(username);
        }
        return null;
    }
}

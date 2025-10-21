package dataaccess;

import model.RegisterRequest;
import model.UserData;

import java.awt.*;
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

    public UserData createUser(RegisterRequest registerRequest){

        try {
            UserData user = new UserData(registerRequest.username(),
                    registerRequest.password(),
                    registerRequest.email());

            users.put(registerRequest.username(), user);

            return user;
        } catch (Exception ex) {
            return null;
        }
    }
}

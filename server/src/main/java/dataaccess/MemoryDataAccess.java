package dataaccess;

import model.AuthData;
import model.ClearRequest;
import model.UserData;

import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    final private HashMap<String, UserData> users = new HashMap<>();


    final private HashMap<String, AuthData> authorizations = new HashMap<>();

    public HashMap<String, UserData> getUsers() {
        return users;
    }
    public HashMap<String, AuthData> getAuthorizations() {
        return authorizations;
    }

    public UserData getUser(String username) {
        return users.getOrDefault(username, null);
    }

    public void createUser(UserData user){
        users.put(user.username(), user);
    }

    public void createAuth(AuthData authData ){
//        authorizations.put(authData.username(), authData);
        authorizations.put(authData.authToken(), authData);
    }

    public AuthData getAuth(String authToken){
        return authorizations.getOrDefault(authToken, null);
    }

    public void deleteAuth(AuthData authData){
        AuthData removedAuthData = authorizations.remove(authData.authToken());
    }

    public void clear(ClearRequest request){
        users.clear();
        authorizations.clear();
    }
}

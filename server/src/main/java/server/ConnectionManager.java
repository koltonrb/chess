package server;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionManager {
    private ConcurrentHashMap<Integer, CopyOnWriteArrayList<Session>> sessionsInGame = new ConcurrentHashMap<>();

    public void saveSession(Integer gameID, Session session){
        sessionsInGame.computeIfAbsent(gameID, theGameID -> new CopyOnWriteArrayList<>()).add(session);
    }

    public void removeSession(Integer gameID, Session session){
        CopyOnWriteArrayList<Session> pointerToList = sessionsInGame.get(gameID);
        if (pointerToList != null){
            pointerToList.remove(session);

            if (pointerToList.isEmpty()) {
                sessionsInGame.remove(gameID);
            }
        }
    }

    public List<Session> getSessions(Integer gameID){
        return sessionsInGame.getOrDefault(gameID, CopyOnWriteArrayList.of());
    }

    public void broadcast(Integer gameID, Session excludeSession, ServerMessage message) throws IOException {
        String msg = new Gson().toJson(message);
        for (Session c : getSessions(gameID)){
            if (c.isOpen()) {
                if (!c.equals(excludeSession)){
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}

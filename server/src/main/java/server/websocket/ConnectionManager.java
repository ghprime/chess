package server.websocket;

import com.google.gson.Gson;
import dataaccess.exception.DataAccessException;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {
    private final Map<Integer, Map<String, Session>> connections = new HashMap<>();
    private final Gson gson = new Gson();

    private static ConnectionManager instance;

    public static ConnectionManager getInstance() {
        if (instance == null) instance = new ConnectionManager();
        return instance;
    }

    public ConnectionManager() {
        if (instance == null) {
            instance = this;
        }
    }

    public void addConnection(UserGameCommand command, Session session) {
        addConnection(command.getGameID(), command.getAuthToken(), session);
    }

    public void addConnection(int gameID, String authToken, Session session) {
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new HashMap<>());
        }
        connections.get(gameID).put(authToken, session);
    }

    public void removeConnection(UserGameCommand command) {
        removeConnection(command.getGameID(), command.getAuthToken());
    }

    public void removeConnection(int gameID, String authToken) {
        connections.get(gameID).remove(authToken);
        if (connections.get(gameID).isEmpty()) connections.remove(gameID);
    }

    /**
     * Broadcasts the message to everything *BUT* the current connection
     */
    public void broadcastMessage(UserGameCommand command, ServerMessage message) throws DataAccessException {
        broadcastMessage(command.getGameID(), command.getAuthToken(), message);
    }

    /**
     * Broadcasts the message to everything *BUT* the current connection
     */
    public void broadcastMessage(int gameID, String authToken, ServerMessage message) throws DataAccessException {
        Map<String, Session> sessions = connections.get(gameID);

        ArrayList<String> closedSessions = new ArrayList<>();

        for (String sessAuthToken : sessions.keySet()) {
            if (authToken.equals(sessAuthToken)) {
                continue;
            }
            Session session = sessions.get(sessAuthToken);
            if (session.isOpen()) {
                sendMessage(sessions.get(sessAuthToken), message);
            } else {
                closedSessions.add(sessAuthToken);
            }
        }

        for (String sessAuthToken : closedSessions) {
            removeConnection(gameID, sessAuthToken);
        }
    }

    public void sendMessage(UserGameCommand command, ServerMessage message) throws DataAccessException {
        sendMessage(command.getGameID(), command.getAuthToken(), message);
    }

    public void sendMessage(int gameID, String authToken, ServerMessage message) throws DataAccessException {
        Session session = connections.get(gameID).get(authToken);
        sendMessage(session, message);
    }

    public void sendMessage(Session session, ServerMessage message) throws DataAccessException {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}

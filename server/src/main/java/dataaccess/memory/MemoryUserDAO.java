package dataaccess.memory;

import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.UserDAO;
import models.UserData;

import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private final HashMap<String, UserData> users = new HashMap<>();
    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    @Override
    public void insertUser(UserData userData) throws DataAccessException {
        if (users.get(userData.username()) != null) {
            throw new AlreadyTakenException("Username already taken!");
        }
        users.put(userData.username(), userData);
    }

    @Override
    public void clear() throws DataAccessException {
        users.clear();
    }
}

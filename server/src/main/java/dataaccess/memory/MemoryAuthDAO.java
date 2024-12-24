package dataaccess.memory;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import models.AuthData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO {
    private final HashMap<String, AuthData> auths = new HashMap<>();

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        return auths.get(authToken);
    }

    @Override
    public void insertAuthData(AuthData authData) throws DataAccessException {
        if (auths.get(authData.authToken()) != null) throw new DataAccessException("Auth token already exists!");
        auths.put(authData.authToken(), authData);
    }

    @Override
    public void deleteAuthData(String authToken) throws DataAccessException {
        if (auths.get(authToken) == null) return;
        auths.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException {
        auths.clear();
    }
}

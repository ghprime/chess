package dataaccess.daointerface;

import dataaccess.exception.DataAccessException;
import models.AuthData;

public interface AuthDAO {
    AuthData getAuthData(String authToken) throws DataAccessException;
    void insertAuthData(AuthData authData) throws DataAccessException;
    void deleteAuthData(String authToken) throws DataAccessException;
    void clear() throws DataAccessException;
}

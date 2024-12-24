package dataaccess.daointerface;

import dataaccess.exception.DataAccessException;
import models.UserData;

public interface UserDAO {
    UserData getUser(String username) throws DataAccessException;
    void insertUser(UserData userData) throws DataAccessException;
    void clear() throws DataAccessException;
}

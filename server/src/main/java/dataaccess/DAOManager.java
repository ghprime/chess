package dataaccess;

import dataaccess.daointerface.AuthDAO;
import dataaccess.daointerface.GameDAO;
import dataaccess.daointerface.UserDAO;
import dataaccess.exception.DataAccessException;

public interface DAOManager {
    void initialize() throws DataAccessException;
    UserDAO getUserDAO();
    AuthDAO getAuthDAO();
    GameDAO getGameDAO();
}

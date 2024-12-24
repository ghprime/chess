package dataaccess.memory;

import dataaccess.*;
import dataaccess.daointerface.AuthDAO;
import dataaccess.daointerface.GameDAO;
import dataaccess.daointerface.UserDAO;
import dataaccess.exception.DataAccessException;

public class MemoryDAOManager implements DAOManager {
    private UserDAO userDAO = null;
    private AuthDAO authDAO = null;
    private GameDAO gameDAO = null;

    @Override
    public void initialize() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
    }

    @Override
    public UserDAO getUserDAO() {
        return userDAO;
    }

    @Override
    public AuthDAO getAuthDAO() {
        return authDAO;
    }

    @Override
    public GameDAO getGameDAO() {
        return gameDAO;
    }
}

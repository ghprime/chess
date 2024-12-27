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

    private static MemoryDAOManager instance = null;

    public static DAOManager getInstance() {
        return instance;
    }

    public MemoryDAOManager() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        if (instance == null) {
            instance = this;
        }
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

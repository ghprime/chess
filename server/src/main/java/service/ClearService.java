package service;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.GameDAO;
import dataaccess.daointerface.UserDAO;

public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ClearService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void clear() throws DataAccessException {
        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}

package dataaccess;

import dataaccess.daointerface.AuthDAO;
import dataaccess.daointerface.GameDAO;
import dataaccess.daointerface.UserDAO;

public interface DAOManager {
    UserDAO getUserDAO();
    AuthDAO getAuthDAO();
    GameDAO getGameDAO();
}

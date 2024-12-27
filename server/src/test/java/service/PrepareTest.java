package service;

import dataaccess.DAOManager;
import dataaccess.exception.DataAccessException;
import dataaccess.sql.SQLDAOManager;
import service.server.ServiceUtils;

public class PrepareTest {
    public static DAOManager prepareTest() throws DataAccessException {
        DAOManager dao = new SQLDAOManager();
        dao.getUserDAO().clear();
        dao.getAuthDAO().clear();
        dao.getGameDAO().clear();
        ServiceUtils.setAuthDAO(dao.getAuthDAO());
        return dao;
    }
}

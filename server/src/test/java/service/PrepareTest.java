package service;

import dataaccess.DAOManager;
import dataaccess.sql.SQLDAOManager;

public class PrepareTest {
    public static DAOManager prepareTest() {
        try {
            DAOManager dao = new SQLDAOManager();
            dao.initialize();
            dao.getUserDAO().clear();
            dao.getAuthDAO().clear();
            dao.getGameDAO().clear();
            ServiceUtils.setAuthDAO(dao.getAuthDAO());
            return dao;
        } catch (Exception err) {
            System.out.println("Error: " + err.getMessage());
            return null;
        }
    }
}

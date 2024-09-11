package service;

import dataaccess.DatabaseAccess;
import dataaccess.MySqlDAO;

public class PrepareTest {
    public static DatabaseAccess prepareTest() {
        try {
            var dao= MySqlDAO.getInstance();
            var testingService=new TestingService(dao);
            testingService.clear();
            return dao;
        } catch (Exception err) {
            System.out.println("Error: " + err.getMessage());
            return null;
        }
    }
}

package service;

import dataaccess.DatabaseAccess;
import dataaccess.MemoryDAO;

public class PrepareTest {
    public static DatabaseAccess prepareTest() {
        try {
            var dao= MemoryDAO.getInstance();
            var testingService=new TestingService(dao);
            testingService.clear();
            return dao;
        } catch (Exception err) {
            System.out.println("Error: " + err.getMessage());
            return null;
        }
    }
}

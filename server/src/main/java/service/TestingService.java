package service;

import dataaccess.DataAccessException;
import dataaccess.DatabaseAccess;

public class TestingService {
  DatabaseAccess dao;

  public TestingService(DatabaseAccess dao) {
    this.dao=dao;
  }

  /**
   * Clears all data from the server
   */
  public void clear() throws DataAccessException {
    dao.clear();
  }
}

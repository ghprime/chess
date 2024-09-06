package service;

import dataaccess.DataAccessException;
import dataaccess.DatabaseAccess;
import dataaccess.MemoryDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TestingServiceTest {
  DatabaseAccess dao;

  @BeforeEach
  void prepTest() {
    try {
      dao=MemoryDAO.getInstance();
    } catch (DataAccessException e) {
        throw new RuntimeException(e);
    }
  }

  @Test
  void clear() {
    var testingService=new TestingService(dao);

    assertDoesNotThrow(testingService::clear);
  }
}
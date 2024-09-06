package service;

import dataaccess.DataAccessException;
import dataaccess.DatabaseAccess;
import dataaccess.MemoryDAO;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
  DatabaseAccess dao;

  @BeforeEach
  void prepTest() {
    try {
      dao=MemoryDAO.getInstance();
      var testingService=new TestingService(dao);
      testingService.clear();
    } catch (Exception err) {
      System.out.println("Error: " + err.getMessage());
    }
  }

  @Test
  void registerUserSuccess() {
    var user=new User("username", "password", "email");

    var userService=new UserService(dao);

    assertDoesNotThrow(() -> userService.registerUser(user));
  }

  @Test
  void registerUserAlreadyExists() {
    var user=new User("username", "password", "email");

    var userService=new UserService(dao);

    assertDoesNotThrow(() -> userService.registerUser(user));
    var err=assertThrows(DataAccessException.class, () -> userService.registerUser(user));

    assertEquals("already taken", err.getMessage());
  }
}

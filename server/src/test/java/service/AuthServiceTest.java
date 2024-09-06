package service;

import dataaccess.DataAccessException;
import dataaccess.DatabaseAccess;
import dataaccess.MemoryDAO;
import models.AuthToken;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
  DatabaseAccess dao;

  @BeforeEach
  void prepTest() {
    dao = PrepareTest.prepareTest();
  }

  @Test
  void loginSuccess() {
    var user=new User("username", "password", "email");

    var authService=new AuthService(dao);

    var userService=new UserService(dao);

    assertDoesNotThrow(() -> userService.registerUser(user));

    var authToken=assertDoesNotThrow(() -> authService.login(user));

    assertNotNull(authToken);
    assertNotNull(authToken.authToken());
    assertNotNull(authToken.username());
  }

  @Test
  void loginFailureNoSuchUser() {
    var user=new User("username", null, "email");

    var authService=new AuthService(dao);

    var err=assertThrows(DataAccessException.class, () -> authService.login(user));

    assertEquals("unauthorized", err.getMessage());
  }

  @Test
  void logoutSuccess() {
    var user=new User("username", "password", "email");

    var authService=new AuthService(dao);

    var userService=new UserService(dao);

    assertDoesNotThrow(() -> userService.registerUser(user));

    var authToken=assertDoesNotThrow(() -> authService.login(user));

    assertDoesNotThrow(() -> authService.logout(authToken));
  }

  @Test
  void logoutNoAuthFailure() {
    var authService=new AuthService(dao);
    var authToken=new AuthToken("username");

    var err=assertThrows(DataAccessException.class, () -> authService.logout(authToken));

    assertEquals("unauthorized", err.getMessage());
  }
}
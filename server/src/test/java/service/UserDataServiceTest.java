package service;

import dataaccess.DAOManager;
import dataaccess.exception.AlreadyTakenException;
import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDataServiceTest {
  DAOManager dao;
  RegisterService registerService;
  UserData userData;
  AuthData authData;

  @BeforeEach
  void prepTest() throws Exception {
    userData = new UserData(
            "username",
            "password",
            "email"
    );
    authData = new AuthData(userData.username());
    dao = PrepareTest.prepareTest();
    if (dao == null) throw new Exception("DAO could not initialize");

    registerService = new RegisterService(dao.getUserDAO(), dao.getAuthDAO());
  }

  @Test
  void registerUserSuccess() {
    assertDoesNotThrow(() -> registerService.register(userData));
  }

  @Test
  void registerUserAlreadyExists() {
    assertDoesNotThrow(() -> dao.getUserDAO().insertUser(userData));
    assertThrows(AlreadyTakenException.class, () -> registerService.register(userData));
  }
}

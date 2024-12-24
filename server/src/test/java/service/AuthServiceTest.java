package service;

import dataaccess.DAOManager;
import dataaccess.exception.UnauthorizedException;
import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
  DAOManager dao;
  LoginService loginService;
  LogoutService logoutService;
  UserData userData;
  AuthData authData;

  @BeforeEach
  void prepTest() throws Exception {
    userData = new UserData(
            "username",
            BCrypt.hashpw("password", BCrypt.gensalt()),
            "email"
    );
    authData = new AuthData(userData.username());
    dao = PrepareTest.prepareTest();
    if (dao == null) throw new Exception("DAO could not initialize");

    loginService = new LoginService(dao.getUserDAO(), dao.getAuthDAO());
    logoutService = new LogoutService(dao.getAuthDAO());
  }

  @Test
  void loginSuccess() {
    assertDoesNotThrow(() -> dao.getUserDAO().insertUser(userData));
    authData = assertDoesNotThrow(() -> loginService.login(
            new UserData(
                    userData.username(),
                    "password",
                    userData.email()
            )
    ));
    assertNotNull(authData);
    assertNotNull(authData.authToken());
  }

  @Test
  void loginFailureNoSuchUser() {
    assertThrows(UnauthorizedException.class, () -> loginService.login(userData));
  }

  @Test
  void logoutSuccess() {
    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));
    assertDoesNotThrow(() -> logoutService.logout(authData));
  }

  @Test
  void logoutNoAuthFailure() {
    assertThrows(UnauthorizedException.class, () -> logoutService.logout(authData));
  }
}

package dataaccess;

import chess.ChessGame;
import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.DataAccessException;
import dataaccess.memory.MemoryDAOManager;
import dataaccess.sql.SQLDAOManager;
import models.AuthData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DAOTests {
  UserData userData;
  AuthData authData;
  GameData gameData;

  @BeforeEach
  void prepTest() {
    userData = new UserData("username", "password", "email");
    authData = new AuthData(userData.username());
    gameData = new GameData("game name");
  }

  DAOManager instantiateDatabase(Class<DAOManager> daoManager) {
    try {
      DAOManager dao=daoManager.getDeclaredConstructor().newInstance();
      dao.getUserDAO().clear();
      dao.getAuthDAO().clear();
      dao.getGameDAO().clear();
      return dao;
    } catch (Exception err) {
      System.out.println("Error: " + err.getMessage());
    }
    return null;
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void clearUsers(Class<DAOManager> daoClass) {
    DAOManager dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getUserDAO().clear());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void clearAuth(Class<DAOManager> daoClass) {
    DAOManager dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getAuthDAO().clear());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void clearGames(Class<DAOManager> daoClass) {
    DAOManager dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getGameDAO().clear());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void insertUserSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getUserDAO().insertUser(userData));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void insertUserAlreadyExists(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getUserDAO().insertUser(userData));

    assertThrows(AlreadyTakenException.class, () -> dao.getUserDAO().insertUser(userData));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void getUserSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getUserDAO().insertUser(userData));

    UserData returnedUser = assertDoesNotThrow(() -> dao.getUserDAO().getUser(userData.username()));

    assertEquals(returnedUser, userData);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void getUserRetrieveNull(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getUserDAO().insertUser(userData));

    UserData returnedUser = assertDoesNotThrow(() -> dao.getUserDAO().getUser(userData.username()));

    assertEquals(returnedUser, userData);

    UserData nullUser = assertDoesNotThrow(() -> dao.getUserDAO().getUser("notAUsername"));

    assertNull(nullUser);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void insertAuthDataSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void insertAuthDataFailure(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));
    assertThrows(DataAccessException.class, () -> dao.getAuthDAO().insertAuthData(authData));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void getAuthDataSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));

    AuthData retrievedAuth = assertDoesNotThrow(() -> dao.getAuthDAO().getAuthData(authData.authToken()));

    assertEquals(retrievedAuth, authData);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void getAuthDataRetrieveNull(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    AuthData retrievedAuth = assertDoesNotThrow(() -> dao.getAuthDAO().getAuthData(authData.authToken()));

    assertNull(retrievedAuth);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void deleteAuthDataSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));

    assertDoesNotThrow(() -> dao.getAuthDAO().deleteAuthData(authData.authToken()));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void deleteAuthDataNonExistent(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getAuthDAO().deleteAuthData(authData.authToken()));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void insertGameSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void insertGameMultipleOfSameName(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    int gameID1 = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));
    int gameID2 = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    assertNotEquals(gameID1, gameID2);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void getGameSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    int gameID = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    GameData retrievedGameData = assertDoesNotThrow(() -> dao.getGameDAO().getGameData(gameID));

    assertEquals(gameID, retrievedGameData.gameID());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void getGameReturnNull(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    GameData retrievedGameData = assertDoesNotThrow(() -> dao.getGameDAO().getGameData(1));

    assertNull(retrievedGameData);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void updateGameSuccess(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    int gameID = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    GameData retrievedGameData = assertDoesNotThrow(() -> dao.getGameDAO().getGameData(gameID));

    assertEquals(gameID, retrievedGameData.gameID());

    gameData = new GameData(gameID, "WHITE", null, gameData.gameName(), new ChessGame());

    assertDoesNotThrow(() -> dao.getGameDAO().updateGameData(gameData));

    retrievedGameData = assertDoesNotThrow(() -> dao.getGameDAO().getGameData(gameID));

    assertEquals(gameData, retrievedGameData);
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void updateGameNotExists(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    assertThrows(DataAccessException.class, () -> dao.getGameDAO().updateGameData(gameData));
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void listGamesSuccessEmpty(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    List<GameData> games = assertDoesNotThrow(() -> dao.getGameDAO().listGames());

    assertTrue(games.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(classes = {MemoryDAOManager.class, SQLDAOManager.class})
  void listGamesSuccessWithGames(Class<DAOManager> daoClass) {
    var dao=instantiateDatabase(daoClass);

    int gameID = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    List<GameData> games = assertDoesNotThrow(() -> dao.getGameDAO().listGames());

    assertEquals(games.getFirst().gameID(), gameID);
  }
}

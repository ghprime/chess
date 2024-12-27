package service;

import chess.ChessGame;
import dataaccess.DAOManager;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.UnauthorizedException;
import models.AuthData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.*;
import service.server.CreateGameService;
import service.server.JoinGameService;
import service.server.ListGamesService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameDataServiceTest {
  UserData userData;
  AuthData authData;
  GameData gameData;
  DAOManager dao;
  CreateGameService createGameService;
  ListGamesService listGamesService;
  JoinGameService joinGameService;

  @BeforeEach
  void prepTest() throws DataAccessException {
    userData = new UserData("username", "password");
    authData = new AuthData(userData.username());
    gameData = new GameData("game name");

    dao = PrepareTest.prepareTest();

    createGameService = new CreateGameService(dao.getGameDAO());
    listGamesService = new ListGamesService(dao.getGameDAO());
    joinGameService = new JoinGameService(dao.getGameDAO());
  }

  @Test
  void listGamesSuccess() {
    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));
    int gameID = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    List<GameData> games = assertDoesNotThrow(() -> listGamesService.listGames(authData));

    assertEquals(games.getFirst().gameID(), gameID);
  }

  @Test
  void listGamesUnauthorized() {
    assertThrows(UnauthorizedException.class, () -> listGamesService.listGames(authData));
  }

  @Test
  void createGameSuccess() {
    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));

    assertDoesNotThrow(() -> createGameService.createGame("game name", authData));
  }

  @Test
  void createGameUnauthorized() {
    assertThrows(UnauthorizedException.class, () -> createGameService.createGame("game name", authData));
  }

  @Test
  void joinGameSuccess() {
    assertDoesNotThrow(() -> dao.getAuthDAO().insertAuthData(authData));
    int gameID = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    assertDoesNotThrow(() -> joinGameService.joinGame(gameID, ChessGame.TeamColor.WHITE, authData));
  }

  @Test
  void joinGameUnauthorized() {
    int gameID = assertDoesNotThrow(() -> dao.getGameDAO().insertGameData(gameData));

    assertThrows(UnauthorizedException.class, () -> joinGameService.joinGame(gameID, ChessGame.TeamColor.WHITE, authData));
  }
}

package service;

import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.GameDAO;
import models.AuthData;
import models.GameData;

public class CreateGameService {
    private final GameDAO gameDAO;

    public CreateGameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public int createGame(String gameName, AuthData authData) throws DataAccessException {
        ServiceUtils.verifyAuthData(authData);

        GameData newGame = new GameData(gameName);

        return gameDAO.insertGameData(newGame);
    }
}

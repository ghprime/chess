package service;

import dataaccess.daointerface.AuthDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.daointerface.GameDAO;
import models.AuthData;
import models.GameData;

import java.util.List;

public class ListGamesService {
    private final GameDAO gameDAO;

    public ListGamesService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public List<GameData> listGames(AuthData authData) throws DataAccessException {
        ServiceUtils.verifyAuthData(authData);

        return gameDAO.listGames();
    }
}

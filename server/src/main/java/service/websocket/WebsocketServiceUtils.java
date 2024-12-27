package service.websocket;

import dataaccess.daointerface.AuthDAO;
import dataaccess.daointerface.GameDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.UnauthorizedException;
import models.AuthData;
import models.GameData;
import dataaccess.exception.BadRequestException;

public class WebsocketServiceUtils {
    private static AuthDAO authDAO = null;
    private static GameDAO gameDAO = null;

    public static void setDAOs(AuthDAO authDAO, GameDAO gameDAO) {
        WebsocketServiceUtils.authDAO = authDAO;
        WebsocketServiceUtils.gameDAO = gameDAO;
    }

    public static AuthData validateAuthToken(String authToken) throws DataAccessException {
        if (authDAO == null) {
            throw new DataAccessException("AuthDAO has not been initialized!");
        }

        if (authToken == null) {
            throw new BadRequestException();
        }

        AuthData returnedAuthData = authDAO.getAuthData(authToken);

        if (returnedAuthData == null) {
            throw new UnauthorizedException("Unauthorized!");
        }

        return returnedAuthData;
    }

    public static GameData getGame(int gameID) throws DataAccessException {
        if (gameDAO == null) {
            throw new DataAccessException("GameDAO has not been initialized!");
        }
        return gameDAO.getGameData(gameID);
    }
}

package service.websocket;

import dataaccess.daointerface.GameDAO;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import server.websocket.ConnectionManager;
import websocket.commands.ResignCommand;
import websocket.messages.NotificationMessage;

import static service.websocket.WebsocketServiceUtils.validateAuthToken;

public class ResignService {
    private final ConnectionManager conns;
    private final GameDAO gameDAO;

    public ResignService(ConnectionManager conns, GameDAO gameDAO) {
        this.conns = conns;
        this.gameDAO = gameDAO;
    }

    public void resign(ResignCommand command) throws DataAccessException {
        AuthData authData = validateAuthToken(command.getAuthToken());

        GameData game = gameDAO.getGameData(command.getGameID());

        String color;

        if (authData.username().equals(game.whiteUsername())) {
            color = "WHITE";
        } else if (authData.username().equals(game.blackUsername())) {
            color = "BLACK";
        } else {
            throw new DataAccessException("Cannot resign as an observer!");
        }

        if (game.gameOver()) {
            throw new DataAccessException("Cannot resign if the game is over!");
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game(),
                true
        );

        gameDAO.updateGameData(updatedGame);

        NotificationMessage message = new NotificationMessage(
                String.format(
                        "%s (%s) has resigned.",
                        authData.username(),
                        color
                )
        );

        conns.broadcastMessage(command, message);
        conns.sendMessage(command, message);
    }
}

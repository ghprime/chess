package service.websocket;

import chess.ChessGame;
import dataaccess.daointerface.GameDAO;
import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.BadRequestException;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import websocket.commands.ConnectPlayerCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class ConnectPlayerService {
    private final ConnectionManager conns;
    private final GameDAO gameDAO;

    public ConnectPlayerService(ConnectionManager conns, GameDAO gameDAO) {
        this.conns = conns;
        this.gameDAO = gameDAO;
    }

    public void connectPlayer(Session session, ConnectPlayerCommand command) throws DataAccessException {
        GameData game = gameDAO.getGameData(command.getGameID());

        if (command.getPlayerColor() == null) {
            throw new BadRequestException();
        }

        AuthData authData = this.conns.addConnection(command, session);


        GameData updatedGame;
        String color;

        if (command.getPlayerColor() == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("Player WHITE already filled!");
            }

            updatedGame = new GameData(
                    game.gameID(),
                    authData.username(),
                    game.blackUsername(),
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            );

            color = "WHITE";
        } else if (command.getPlayerColor() == ChessGame.TeamColor.BLACK) {
            if (game.blackUsername() != null) {
                throw new AlreadyTakenException("Player BLACK already filled!");
            }

            updatedGame = new GameData(
                    game.gameID(),
                    game.whiteUsername(),
                    authData.username(),
                    game.gameName(),
                    game.game(),
                    game.gameOver()
            );

            color = "BLACK";
        } else {
            throw new DataAccessException("Invalid player color!");
        }

        gameDAO.updateGameData(updatedGame);

        this.conns.broadcastMessage(command, new NotificationMessage(
                String.format(
                        "%s has connected as the %s player",
                        authData.username(),
                        color)
                )
        );

        this.conns.sendMessage(command, new LoadGameMessage(updatedGame.game()));
    }
}

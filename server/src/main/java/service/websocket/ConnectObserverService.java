package service.websocket;

import dataaccess.daointerface.GameDAO;
import dataaccess.exception.BadRequestException;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import websocket.commands.ConnectObserverCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

public class ConnectObserverService {
    private final ConnectionManager conns;
    private final GameDAO gameDAO;

    public ConnectObserverService(ConnectionManager conns, GameDAO gameDAO) {
        this.conns = conns;
        this.gameDAO = gameDAO;
    }

    public void connectObserver(Session session, ConnectObserverCommand command) throws DataAccessException {
        GameData game = gameDAO.getGameData(command.getGameID());

        if (game == null) {
            throw new BadRequestException();
        }

        AuthData authData = this.conns.addConnection(command, session);

        this.conns.broadcastMessage(command, new NotificationMessage(String.format("%s is now observing the game", authData.username())));

        this.conns.sendMessage(command, new LoadGameMessage(game.game()));
    }
}

package server.websocket;

import com.google.gson.Gson;
import dataaccess.DAOManager;
import dataaccess.daointerface.AuthDAO;
import dataaccess.daointerface.GameDAO;
import dataaccess.exception.DataAccessException;
import dataaccess.sql.SQLDAOManager;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.websocket.*;
import websocket.commands.*;
import websocket.messages.ErrorMessage;

import java.io.IOException;

@WebSocket
public class WebsocketHandler {
    ConnectionManager conns;
    ConnectObserverService connectObserverService;
    ConnectPlayerService connectPlayerService;
    LeaveService leaveService;
    MakeMoveService makeMoveService;
    ResignService resignService;

    public WebsocketHandler() {
        this.conns = ConnectionManager.getInstance();
        DAOManager manager = SQLDAOManager.getInstance();
        this.connectObserverService = new ConnectObserverService(conns);
        this.connectPlayerService = new ConnectPlayerService(conns, manager.getGameDAO());
        this.leaveService = new LeaveService(conns, manager.getGameDAO());
        this.makeMoveService = new MakeMoveService(conns, manager.getGameDAO());
        this.resignService = new ResignService(conns, manager.getGameDAO());
        WebsocketServiceUtils.setDAOs(manager.getAuthDAO(), manager.getGameDAO());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            var genericCommand=new Gson().fromJson(message, websocket.commands.UserGameCommand.class);

            if (genericCommand.getAuthToken() == null
                    || genericCommand.getCommandType() == null
                    || genericCommand.getGameID() == null
            ) {
                conns.sendMessage(session, new ErrorMessage("Error: Unauthorized!"));
                return;
            }

            switch (genericCommand.getCommandType()) {
                case CONNECT:
                    if (message.contains("playerColor")) {
                        connectPlayerService.connectPlayer(session, fromJson(message, ConnectPlayerCommand.class));
                    } else {
                        connectObserverService.connectObserver(session, fromJson(message, ConnectObserverCommand.class));
                    }
                    break;
                case MAKE_MOVE:
                    makeMoveService.makeMove(fromJson(message, MakeMoveCommand.class));
                    break;
                case LEAVE:
                    leaveService.leave(fromJson(message, LeaveCommand.class));
                    break;
                case RESIGN:
                    resignService.resign(fromJson(message, ResignCommand.class));
                    break;
                default:
                    conns.sendMessage(session, new ErrorMessage("Unknown message type!"));
                    break;
            }
        } catch (DataAccessException e) {
            try {
                conns.sendMessage(session, new ErrorMessage("Error: " + e.getMessage()));
            } catch (DataAccessException e1) {
                System.out.println(e1.getMessage());
            }
        }
    }

    private <T> T fromJson(String json, Class<T> type) {
        return new Gson().fromJson(json, type);
    }
}

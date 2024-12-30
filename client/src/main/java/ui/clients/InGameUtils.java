package ui.clients;

import chess.ChessPiece;
import chess.ChessPosition;
import serverfacade.WebsocketFacade;
import ui.ChessClient;
import ui.ClientException;
import ui.State;

public class InGameUtils {
    static String leave(WebsocketFacade ws, ChessClient client) throws ClientException {
        ws.leave();

        client.closeWs();
        client.setState(State.SIGNED_IN);
        client.setCurrentGame(null);
        client.setTeamColor(null);

        return "Successfully left the game!";
    }

    static String redraw(ChessClient client) {
        return client.displayGame(client.getCurrentGame());
    }

    static String highlight(ChessClient client, String[] params) throws ClientException {
        if (params.length != 1 || params[0].length() != 2) {
            throw new ClientException(400, "Expected: <file><rank>");
        }

        char file=params[0].charAt(0);
        char rank=params[0].charAt(1);

        if (file < 97 || file > 104 || rank < 49 || rank > 56) {
            throw new ClientException(400, "Expected: <[a-h]><[1-8]> <[a-h]><[1-8]>");
        }

        ChessPosition position=new ChessPosition(rank - 48, file - 96);

        ChessPiece piece=client.getCurrentGame().getBoard().getPiece(position);

        if (piece == null) {
            throw new ClientException(400, "Not a piece!");
        }

        return client.displayGame(client.getCurrentGame(), position);
    }
}

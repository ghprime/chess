package ui.clients;

import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.InvalidMoveException;
import serverfacade.WebsocketFacade;
import ui.ChessClient;
import ui.ClientException;

enum InGameState {
    NORMAL,
    AWAITING_CONFIRMATION
}

public class InGameEvaluator implements Evaluator {
    private final ChessClient client;
    private final WebsocketFacade ws;
    private InGameState state;

    public InGameEvaluator(ChessClient client) {
        this.client = client;
        ws = client.getWs();
        this.state = InGameState.NORMAL;
    }

    @Override
    public String eval(String command, String[] params) throws ClientException {
        if (state == InGameState.NORMAL) {
            return switch (command) {
                case "redraw" -> redraw();
                case "resign" -> resign();
                case "leave" -> leave();
                case "move" -> move(params);
                case "highlight" -> highlight(params);
                default -> null;
            };
        } else {
            return checkConfirmation(command);
        }
    }

    private String checkConfirmation(String command) throws ClientException {
        state = InGameState.NORMAL;

        if ("yes".equals(command)) {
            ws.resign();
            return "Resigning...";
        }

        return "Resignation canceled!";
    }

    private String highlight(String[] params) throws ClientException {
        return InGameUtils.highlight(client, params);
    }

    private String move(String[] params) throws ClientException {
        String genericError = "Expected: <file><rank> <file><rank>";
        if (params.length != 2) {
            throw new ClientException(400, genericError);
        }

        String from=params[0].toLowerCase();
        String to=params[1].toLowerCase();

        if (from.length() != 2 || to.length() != 2) {
            throw new ClientException(400, genericError);
        }

        ChessPosition[] positions=new ChessPosition[]{null, null};

        int index=0;

        for (String pos : new String[]{from, to}) {
            char file=pos.charAt(0);
            char rank=pos.charAt(1);

            if (file < 97 || file > 104 || rank < 49 || rank > 56) {
                throw new ClientException(400, "Expected: <[a-h]><[1-8]> <[a-h]><[1-8]>");
            }

            positions[index++]=new ChessPosition(rank - 48, file - 96);
        }

        try {
            ChessPiece piece=client.getCurrentGame().getBoard().getPiece(positions[0]);

            if (piece != null && (piece.getTeamColor() != client.getTeamColor())) {
                throw new ClientException(400, "Wrong team piece!");
            }

            ChessMove move=new ChessMove(positions[0], positions[1]);

            client.getCurrentGame().makeMove(move);
            ws.makeMove(move);
            return "Successfully moved!";
        } catch (InvalidMoveException ex) {
            String message = !ex.getMessage().isEmpty() ? ex.getMessage() : "Invalid move!";
            throw new ClientException(400, message);
        }
    }

    private String leave() throws ClientException {
        return InGameUtils.leave(ws, client);
    }

    private String resign() {
        state = InGameState.AWAITING_CONFIRMATION;

        return "Are you sure you want to resign? yes/no";
    }

    private String redraw() {
        return InGameUtils.redraw(client);
    }

    @Override
    public String help() {
        return """
              - move <file><rank> <file><rank>
              - highlight <file><rank>
              - redraw
              - leave
              - resign
              - help
              """;
    }
}

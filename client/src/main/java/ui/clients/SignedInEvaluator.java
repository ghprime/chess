package ui.clients;

import chess.ChessGame;
import models.GameData;
import serverfacade.ServerFacade;
import ui.ChessClient;
import ui.ClientException;
import ui.State;

import java.util.List;

public class SignedInEvaluator implements Evaluator {
    private final ChessClient client;
    private final ServerFacade server;
    private List<GameData> games = null;

    public SignedInEvaluator(ChessClient client) {
        this.client = client;
        this.server = client.getServer();
    }

    @Override
    public String eval(String command, String[] params) throws ClientException {
        return switch (command) {
            case "list" -> listGames();
            case "create" -> createGame(params);
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
            case "logout" -> logout();
            default -> null;
        };
    }

    private String listGames() throws ClientException {
        games=server.listGames(client.getAuthData());

        if (games.isEmpty()) {
            return "No games.";
        }

        var sb=new StringBuilder();

        var index=0;

        for (var game : games) {
            sb.append(++index);
            sb.append(") Name: '");
            sb.append(game.gameName());
            sb.append("'; White player: ");
            if (game.whiteUsername() != null) {
                sb.append("'").append(game.whiteUsername()).append("'");
            }
            else {
                sb.append("Empty");
            }
            sb.append("; Black player: ");
            if (game.blackUsername() != null) {
                sb.append("'").append(game.blackUsername()).append("'");
            }
            else {
                sb.append("Empty");
            }
            sb.append(";");
            if (index != games.size()) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String createGame(String[] params) throws ClientException {
        if (params.length == 0) {
            throw new ClientException(400, "Expected: create <name>");
        }

        server.createGame(client.getAuthData(), String.join(" ", params));
        return "Successfully created game!";
    }

    private int getGameID(String rawGameID, String errorMessage) throws ClientException {
        if (games == null) {
            throw new ClientException("Must list games first!");
        }

        int id;
        try {
            id=Integer.parseInt(rawGameID);
        } catch (NumberFormatException e) {
            throw new ClientException(errorMessage);
        }

        id -= 1;

        if (id >= games.size() || id < 0) {
            throw new ClientException("No such game!");
        }

        return games.get(id).gameID();
    }

    private String joinGame(String[] params) throws ClientException {
        String errorMessage = "Expected: <game number> <BLACK/WHITE>";
        if (params.length != 2) {
            throw new ClientException(400, errorMessage);
        }

        int gameID = getGameID(params[0], errorMessage);

        client.setGameID(gameID);

        ChessGame.TeamColor teamColor;

        if ("BLACK".equals(params[1])) {
            teamColor = ChessGame.TeamColor.BLACK;
        } else if ("WHITE".equals(params[1])) {
            teamColor = ChessGame.TeamColor.WHITE;
        } else {
            throw new ClientException(errorMessage);
        }

        server.joinGame(client.getAuthData(), gameID, params[1]);

        client.setTeamColor(teamColor);

        client.openWs();
        client.getWs().joinPlayer(teamColor);

        ChessGame currentGame = new ChessGame();
        currentGame.getBoard().resetBoard();

        client.setCurrentGame(currentGame);

        client.setState(State.IN_GAME);

        return "Successfully joined game!";
    }

    private String observeGame(String[] params) throws ClientException {
        String errorMessage = "Expected: <gameID>";
        if (params.length != 1) {
            throw new ClientException(400, errorMessage);
        }

        int gameID = getGameID(params[0], errorMessage);

        client.setGameID(gameID);

        client.openWs();
        client.getWs().joinObserver();

        ChessGame currentGame = new ChessGame();
        currentGame.getBoard().resetBoard();

        client.setCurrentGame(currentGame);

        client.setState(State.OBSERVING);

        return "Successfully joined game!";
    }

    private String logout() throws ClientException {
        server.logout(client.getAuthData());
        client.setState(State.SIGNED_OUT);
        return "Successfully logged out!";
    }

    @Override
    public String help() {
        return """
              - create <name>
              - list
              - join <game number> <BLACK/WHITE>
              - observe <game number>
              - logout
              - help
              """;
    }
}

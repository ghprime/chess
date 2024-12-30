package ui.clients;

import models.AuthData;
import models.UserData;
import serverfacade.ServerFacade;
import ui.ChessClient;
import ui.ClientException;
import ui.State;

public class SignedOutEvaluator implements Evaluator {
    private final ChessClient client;
    private final ServerFacade server;

    public SignedOutEvaluator(ChessClient client) {
        this.client = client;
        this.server = client.getServer();
    }

    @Override
    public String eval(String command, String[] params) throws ClientException {
        return switch (command) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "clear" -> clear();
            case "quit" -> quit();
            default -> null;
        };
    }

    private String quit() {
        return "quit";
    }

    private String clear() throws ClientException {
        server.clear();

        return "Server successfully cleared!";
    }

    private String register(String[] params) throws ClientException {
        if (params.length != 3) {
            throw new ClientException(400, "Expected: <username> <password> <email>");
        }

        AuthData authData = server.registerUser(new UserData(params[0], params[1], params[2]));

        client.setAuthData(authData);
        client.setState(State.SIGNED_IN);

        return "Successfully registered and logged in!";
    }

    private String login(String[] params) throws ClientException {
        if (params.length != 2) {
            throw new ClientException(400, "Expected: <username> <password>");
        }

        AuthData authData = server.login(new UserData(params[0], params[1]));

        client.setAuthData(authData);
        client.setState(State.SIGNED_IN);

        return "Successfully logged in!";
    }

    @Override
    public String help() {
        return  """
              - login <username> <password>
              - register <username> <password> <email>
              - quit
              - help
              """;
    }
}

package ui.clients;

import serverfacade.WebsocketFacade;
import ui.ChessClient;
import ui.ClientException;

public class ObservingEvaluator implements Evaluator {
    private final ChessClient client;
    private final WebsocketFacade ws;

    public ObservingEvaluator(ChessClient client) {
        this.client = client;
        ws = client.getWs();
    }

    @Override
    public String eval(String command, String[] params) throws ClientException {
        return switch (command) {
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "highlight" -> highlight(params);
            default -> null;
        };
    }

    private String highlight(String[] params) throws ClientException {
        return InGameUtils.highlight(client, params);
    }

    private String leave() throws ClientException {
        return InGameUtils.leave(ws, client);
    }

    private String redraw() {
        return InGameUtils.redraw(client);
    }

    @Override
    public String help() {
        return """
              - highlight <file><rank>
              - redraw
              - leave
              - help
              """;
    }
}

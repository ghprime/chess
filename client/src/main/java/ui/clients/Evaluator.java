package ui.clients;

import ui.ClientException;

public interface Evaluator {
    String eval(String command, String[] params) throws ClientException;
    String help();
}

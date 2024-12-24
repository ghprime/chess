package server;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.exception.AlreadyTakenException;
import dataaccess.exception.DataAccessException;
import dataaccess.exception.UnauthorizedException;
import dataaccess.sql.SQLDAOManager;
import models.*;
import service.*;
import spark.*;
import com.google.gson.Gson;

import java.util.*;

import static spark.Spark.*;

public class Server {
    RegisterService registerService;
    LoginService loginService;
    LogoutService logoutService;
    ClearService clearService;
    CreateGameService createGameService;
    ListGamesService listGamesService;
    JoinGameService joinGameService;
    DAOManager daoManager;
    Gson gson = new Gson();

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try {
            daoManager = new SQLDAOManager();
            ServiceUtils.setAuthDAO(daoManager.getAuthDAO());

            registerService = new RegisterService(daoManager.getUserDAO(), daoManager.getAuthDAO());
            loginService = new LoginService(daoManager.getUserDAO(), daoManager.getAuthDAO());
            logoutService = new LogoutService(daoManager.getAuthDAO());
            clearService = new ClearService(daoManager.getUserDAO(), daoManager.getAuthDAO(), daoManager.getGameDAO());
            createGameService = new CreateGameService(daoManager.getGameDAO());
            listGamesService = new ListGamesService(daoManager.getGameDAO());
            joinGameService = new JoinGameService(daoManager.getGameDAO());
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
            return -1;
        }

        exception(DataAccessException.class, this::databaseErrorHandler);
        exception(BadRequestException.class, this::badRequestErrorHandler);
        exception(Exception.class, this::errorHandler);

        post("/user", this::registerUser);

        post("/session", this::login);
        delete("/session", this::logout);

        get("/game", this::listGames);
        post("/game", this::createGame);
        put("/game", this::joinGame);

        delete("/db", this::clear);

        Spark.awaitInitialization();

        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object registerUser(Request req, Response res) throws BadRequestException, DataAccessException {
        UserData user = gson.fromJson(req.body(), UserData.class);

        if (user.username() == null || user.username().isEmpty()
                || user.password() == null || user.password().isEmpty()
                || user.email() == null || user.email().isEmpty()
        ) {
            throw new BadRequestException();
        }

        AuthData authData = registerService.register(user);
        res.status(200);
        res.body(toJSON(authData));
        return toJSON(authData);
    }

    private Object login(Request req, Response res) throws BadRequestException, DataAccessException {
        var user = gson.fromJson(req.body(), UserData.class);

        if (user.username() == null || user.username().isEmpty()
                || user.password() == null || user.password().isEmpty()
        ) {
            throw new BadRequestException();
        }

        AuthData authData = loginService.login(user);
        res.status(200);
        res.body(toJSON(authData));
        return toJSON(authData);
    }

    private Object logout(Request req, Response res) throws BadRequestException, DataAccessException {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");

        if (authTokenString == null || authTokenString.isEmpty()) {
            throw new BadRequestException();
        }

        AuthData authData = new AuthData(authTokenString, "");
        logoutService.logout(authData);
        res.status(200);
        return "{}";
    }

    private Object listGames(Request req, Response res) throws BadRequestException, DataAccessException {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");

        if (authTokenString == null || authTokenString.isEmpty()) {
            throw new BadRequestException();
        }

        AuthData authData = new AuthData(authTokenString, "");
        List<GameData> games = listGamesService.listGames(authData);

        ArrayList<GameInfo> gameInfos = new ArrayList<>();

        for (var game : games) {
            gameInfos.add(GameInfo.fromGame(game));
        }

        res.status(200);
        return toJSON(Collections.singletonMap("games", gameInfos));
    }

    private Object createGame(Request req, Response res) throws BadRequestException, DataAccessException {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");

        if (authTokenString == null || authTokenString.isEmpty()) {
            throw new BadRequestException();
        }

        AuthData authData =new AuthData(authTokenString, "");

        GameData gameData = gson.fromJson(req.body(), GameData.class);

        int gameID=createGameService.createGame(gameData.gameName(), authData);

        res.status(200);

        return toJSON(Collections.singletonMap("gameID", gameID));
    }

    private Object joinGame(Request req, Response res) throws BadRequestException, DataAccessException {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");

        if (authTokenString == null || authTokenString.isEmpty()) {
            throw new BadRequestException();
        }

        AuthData authData =new AuthData(authTokenString, "");

        HashMap<String, Object> body = gson.fromJson(req.body(), HashMap.class);

        if (body.get("gameID") == null) {
            throw new BadRequestException();
        }

        int gameID=(int) Math.round((Double) body.get("gameID"));

        if (gameID < 0) {
            throw new BadRequestException();
        }

        if (body.get("playerColor") == null) {
            throw new BadRequestException();
        }

        String playerColor=(String) body.get("playerColor");

        ChessGame.TeamColor colorWanted = null;

        switch (playerColor) {
            case "WHITE":
                colorWanted = ChessGame.TeamColor.WHITE;
                break;
            case "BLACK":
                colorWanted = ChessGame.TeamColor.BLACK;
                break;
            default:
                throw new BadRequestException();
        };

        joinGameService.joinGame(gameID, colorWanted, authData);

        res.status(200);
        return "{}";
    }

    private Object clear(Request request, Response response) throws DataAccessException {
        clearService.clear();
        response.status(200);
        response.body("{}");
        return "{}";
    }

    private Object errorHandler(Exception err, Request req, Response res) {
        String body = getJSONError(err.getMessage());
        res.type("application/json");
        res.status(500);
        res.body(body);
        return body;
    }

    private Object badRequestErrorHandler(BadRequestException err, Request req, Response res) {
        String body = getJSONError(err.getMessage());
        res.type("application/json");
        res.body(body);
        res.status(400);
        return body;
    }

    private Object databaseErrorHandler(DataAccessException err, Request req, Response res) {
        int status;
        var body=getJSONError(err.getMessage());

        if (err instanceof UnauthorizedException) {
            status = 401;
        } else if (err instanceof AlreadyTakenException) {
            status = 403;
        } else {
            status = 400;
        }

        res.type("application/json");
        res.body(body);
        res.status(status);
        return body;
    }

    private String getJSONError(String message) {
        return gson.toJson(new ErrorResponse(message));
    }

    private String toJSON(Object obj) {
        return gson.toJson(obj);
    }

    private record ErrorResponse(String message) {
        ErrorResponse(String message) {
            this.message="Error: " + message;
        }
    }
}

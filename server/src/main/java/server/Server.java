package server;

import dataaccess.*;
import models.*;
import service.*;
import spark.*;
import com.google.gson.Gson;

import java.util.*;

import static spark.Spark.*;

public class Server {
    AuthService authService;
    UserService userService;
    GameService gameService;
    TestingService testingService;
    DatabaseAccess dao;

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        try {
            dao=MySqlDAO.getInstance();

            authService=new AuthService(dao);
            userService=new UserService(dao);
            gameService=new GameService(dao);
            testingService=new TestingService(dao);
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }

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

    private Object registerUser(Request req, Response res) {
        var user=new Gson().fromJson(req.body(), User.class);
        AuthToken authToken;
        try {
            authToken=userService.registerUser(user);
            res.status(200);
            res.body(toJSON(authToken));
            return toJSON(authToken);
        } catch (DataAccessException err) {
            return databaseErrorHandler(err, req, res);
        }
    }

    private Object login(Request req, Response res) {
        var user=new Gson().fromJson(req.body(), User.class);
        AuthToken authToken;
        try {
            authToken=authService.login(user);
            res.status(200);
            res.body(toJSON(authToken));
            return toJSON(authToken);
        } catch (DataAccessException err) {
            return databaseErrorHandler(err, req, res);
        }
    }

    private Object logout(Request req, Response res) {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
        AuthToken authToken=new AuthToken(authTokenString, "");
        try {
            authService.logout(authToken);
            res.status(200);
            return "{}";
        } catch (DataAccessException err) {
            return databaseErrorHandler(err, req, res);
        }
    }

    private Object listGames(Request req, Response res) {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
        AuthToken authToken=new AuthToken(authTokenString, "");
        try {
            var games=gameService.listGames(authToken);
            ArrayList<GameInfo> gameInfos=new ArrayList<>();
            for (var game : games) {
                gameInfos.add(GameInfo.fromGame(game));
            }
            res.status(200);
            return toJSON(Collections.singletonMap("games", gameInfos));
        } catch (DataAccessException err) {
            return databaseErrorHandler(err, req, res);
        }
    }

    private Object createGame(Request req, Response res) {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
        AuthToken authToken=new AuthToken(authTokenString, "");
        try {
            var game=gameService.createGame(authToken, new Gson().fromJson(req.body(), Game.class));
            res.status(200);
            return toJSON(Collections.singletonMap("gameID", game.gameID()));
        } catch (DataAccessException err) {
            return databaseErrorHandler(err, req, res);
        }
    }

    private Object joinGame(Request req, Response res) {
        String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
        AuthToken authToken=new AuthToken(authTokenString, "");
        try {
            var body=new Gson().fromJson(req.body(), HashMap.class);
            int gameID=(int) Math.round((Double) body.get("gameID"));
            var playerColor=(String) body.get("playerColor");
            var white="WHITE".equals(playerColor) ? playerColor : null;
            var black="BLACK".equals(playerColor) ? playerColor : null;
            var isObserver="OBSERVER".equals(playerColor);
            if (white == null && black == null && !isObserver) {
                return databaseErrorHandler(new DataAccessException("Must specify color"), req, res);
            }
            var game=new Game(gameID, white, black, null, null);
            gameService.joinGame(authToken, game);
            res.status(200);
            return "{}";
        } catch (DataAccessException err) {
            return databaseErrorHandler(err, req, res);
        } catch (NullPointerException err) {
            var body=errorHandler(err, req, res);
            res.status(400);
            return body;
        }
    }

    private Object clear(Request request, Response response) {
        try {
            testingService.clear();
        } catch (Exception err) {
            return errorHandler(err, request, response);
        }
        response.status(200);
        response.body("{}");
        return "{}";
    }

    private Object errorHandler(Exception err, Request req, Response res) {
        var body=getJSONError(err.getMessage());
        res.type("application/json");
        res.status(500);
        res.body(body);
        return body;
    }

    private Object databaseErrorHandler(DataAccessException err, Request req, Response res) {
        int status;
        var body=getJSONError(err.getMessage());

        switch (err.getMessage()) {
            case "unauthorized" -> status=401;
            case "already taken" -> status=403;
            default -> status=400;
        }

        res.type("application/json");
        res.body(body);
        res.status(status);
        return body;
    }

    private String getJSONError(String message) {
        return new Gson().toJson(new ErrorResponse(message));
    }

    private String toJSON(Object obj) {
        return new Gson().toJson(obj);
    }

    private record ErrorResponse(String message) {
        ErrorResponse(String message) {
            this.message="Error: " + message;
        }
    }
}

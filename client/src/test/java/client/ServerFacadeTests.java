package client;

import models.AuthToken;
import models.User;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.ClientException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {
    private static Server server;

    private static ServerFacade facade=new ServerFacade();
    private static User user;

    @BeforeAll
    public static void init() throws ClientException {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
        user = new User("user", "pass", "email");
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() throws ClientException {
        facade.clear();
        server.stop();
    }

    @BeforeEach
    void setup() throws ClientException {
        facade.clear();
    }

    @Test
    void clear() {
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    void registerUserSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.username(), authToken.username());
        assertNotNull(authToken.authToken());
    }

    @Test
    void registerUserAlreadyExists() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.username(), authToken.username());
        assertNotNull(authToken.authToken());
        var ex=assertThrows(ClientException.class, () -> facade.registerUser(user));
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void loginSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.username(), authToken.username());
        assertNotNull(authToken.authToken());

        var loginAuthToken=assertDoesNotThrow(() -> facade.login(user));
        assertNotNull(loginAuthToken);
        assertEquals(user.username(), loginAuthToken.username());
        assertNotNull(loginAuthToken.authToken());

        assertNotEquals(authToken.authToken(), loginAuthToken.authToken());
    }

    @Test
    void loginUnauthorized() {
        var ex=assertThrows(ClientException.class, () -> facade.login(user));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        assertDoesNotThrow(() -> facade.logout(authToken));
        var ex=assertThrows(ClientException.class, () -> facade.listGames(authToken));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutUnauthorized() {
        var authToken=new AuthToken("user");
        var ex=assertThrows(ClientException.class, () -> facade.logout(authToken));

        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void listGamesSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        assertNotNull(authToken);
        assertEquals(user.username(), authToken.username());
        assertNotNull(authToken.authToken());

        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertNotNull(game);
        var games=assertDoesNotThrow(() -> facade.listGames(authToken));
        assertNotNull(games);
        assertEquals(games.getFirst().gameID(), game.gameID());
    }

    @Test
    void listGamesUnauthorized() {
        var authToken=new AuthToken("user");
        var ex=assertThrows(ClientException.class, () -> facade.listGames(authToken));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void createGameSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertNotNull(game);
    }

    @Test
    void createGameUnauthorized() {
        var authToken=new AuthToken("user");
        var ex=assertThrows(ClientException.class, () -> facade.createGame(authToken, "game"));
        assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void joinGameSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> facade.joinGame(authToken, game.gameID(), "WHITE"));
    }

    @Test
    void joinGameSlotTaken() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> facade.joinGame(authToken, game.gameID(), "WHITE"));
        var ex=assertThrows(ClientException.class, () -> facade.joinGame(authToken, game.gameID(), "WHITE"));
        assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void observeGame() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(user));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> facade.joinGame(authToken, game.gameID(), "OBSERVER"));
    }
}

package client;

import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;
import ui.ClientException;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {
    private static Server server;

    private static ServerFacade facade=new ServerFacade();
    private static UserData userData;
    private static final String UNAUTHORIZED = "Error: Unauthorized!";

    @BeforeAll
    public static void init() throws ClientException {
        server = new Server();
        var port = server.run(0);
        facade = new ServerFacade(port);
        userData = new UserData("user", "pass", "email");
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
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        assertNotNull(authToken);
        assertEquals(userData.username(), authToken.username());
        assertNotNull(authToken.authToken());
    }

    @Test
    void registerUserAlreadyExists() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        assertNotNull(authToken);
        assertEquals(userData.username(), authToken.username());
        assertNotNull(authToken.authToken());
        var ex=assertThrows(ClientException.class, () -> facade.registerUser(userData));
        assertEquals("Error: Username already taken!", ex.getMessage());
    }

    @Test
    void loginSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        assertNotNull(authToken);
        assertEquals(userData.username(), authToken.username());
        assertNotNull(authToken.authToken());

        var loginAuthToken=assertDoesNotThrow(() -> facade.login(userData));
        assertNotNull(loginAuthToken);
        assertEquals(userData.username(), loginAuthToken.username());
        assertNotNull(loginAuthToken.authToken());

        assertNotEquals(authToken.authToken(), loginAuthToken.authToken());
    }

    @Test
    void loginUnauthorized() {
        var ex=assertThrows(ClientException.class, () -> facade.login(userData));
        assertEquals(UNAUTHORIZED, ex.getMessage());
    }

    @Test
    void logoutSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        assertDoesNotThrow(() -> facade.logout(authToken));
        var ex=assertThrows(ClientException.class, () -> facade.listGames(authToken));
        assertEquals(UNAUTHORIZED, ex.getMessage());
    }

    @Test
    void logoutUnauthorized() {
        var authToken=new AuthData("user");
        var ex=assertThrows(ClientException.class, () -> facade.logout(authToken));

        assertEquals(UNAUTHORIZED, ex.getMessage());
    }

    @Test
    void listGamesSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        assertNotNull(authToken);
        assertEquals(userData.username(), authToken.username());
        assertNotNull(authToken.authToken());

        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertNotNull(game);
        var games=assertDoesNotThrow(() -> facade.listGames(authToken));
        assertNotNull(games);
        assertEquals(games.getFirst().gameID(), game.gameID());
    }

    @Test
    void listGamesUnauthorized() {
        var authToken=new AuthData("user");
        var ex=assertThrows(ClientException.class, () -> facade.listGames(authToken));
        assertEquals(UNAUTHORIZED, ex.getMessage());
    }

    @Test
    void createGameSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertNotNull(game);
    }

    @Test
    void createGameUnauthorized() {
        var authToken=new AuthData("user");
        var ex=assertThrows(ClientException.class, () -> facade.createGame(authToken, "game"));
        assertEquals(UNAUTHORIZED, ex.getMessage());
    }

    @Test
    void joinGameSuccess() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> facade.joinGame(authToken, game.gameID(), "WHITE"));
    }

    @Test
    void joinGameSlotTaken() {
        var authToken=assertDoesNotThrow(() -> facade.registerUser(userData));
        var game=assertDoesNotThrow(() -> facade.createGame(authToken, "game"));
        assertDoesNotThrow(() -> facade.joinGame(authToken, game.gameID(), "WHITE"));
        var ex=assertThrows(ClientException.class, () -> facade.joinGame(authToken, game.gameID(), "WHITE"));
        assertEquals("Error: Player WHITE is already filled!", ex.getMessage());
    }
}

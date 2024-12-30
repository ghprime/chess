package serverfacade;

import com.google.gson.Gson;
import models.AuthData;
import models.GameData;
import models.GameInfo;
import models.UserData;
import ui.ClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerFacade {
  final String urlString;
  final int port;

  public ServerFacade(String urlString, int port) {
    this.urlString=urlString;
    this.port=port;
  }

  public ServerFacade(int port) {
    this.urlString="localhost";
    this.port=port;
  }

  public ServerFacade() {
    this.urlString="localhost";
    this.port=8080;
  }

  public void clear() throws ClientException {
    makeRequest("DELETE", "db", null, null, null);
  }

  public AuthData registerUser(UserData userData) throws ClientException {
    return makeRequest("POST", "user", userData, AuthData.class, null);
  }

  public AuthData login(UserData userData) throws ClientException {
    return makeRequest("POST", "session", userData, AuthData.class, null);
  }

  public void logout(AuthData authData) throws ClientException {
    makeRequest("DELETE", "session", null, null, authData);
  }

  public List<GameData> listGames(AuthData authData) throws ClientException {
    var games=makeRequest("GET", "game", null, GamesList.class, authData);
    if (games == null) {
      return new ArrayList<>();
    }
    return games.games().stream().map(GameInfo::toGame).toList();
  }

  public GameData createGame(AuthData authData, String gameName) throws ClientException {
    return makeRequest("POST", "game", new GameData(gameName), GameData.class, authData);
  }

  public void joinGame(AuthData authData, int gameID, String playerColor) throws ClientException {
    makeRequest("PUT", "game", new JoinGameRequest(gameID, playerColor), null, authData);
  }

  private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, AuthData authData) throws ClientException {
    try {
      var connection=getConnection(path);
      connection.setRequestMethod(method);
      connection.setReadTimeout(5000);
      if (authData != null) {
        connection.addRequestProperty("Authorization", authData.authToken());
      }

      if (request != null) {
        sendData(request, connection);
      }

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        var response=readData(connection, ErrorResponse.class);
        throw new ClientException(connection.getResponseCode(), response.message());
      }

      if (responseClass == null) {
        return null;
      }

      return readData(connection, responseClass);
    } catch (IOException ex) {
      throw new ClientException(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
    }
  }

  private HttpURLConnection getConnection(String path) throws ClientException, IOException {
    var url=getURL(path);

    return (HttpURLConnection) url.openConnection();
  }

  public URL getURL() throws ClientException {
    return getURL(null);
  }

  public URL getURL(String path) throws ClientException {
    try {
      return new URI("http://" + urlString + ":" + port + (path == null ? "" : ("/" + path))).toURL();
    } catch (URISyntaxException | MalformedURLException ex) {
      throw new ClientException(400, ex.getMessage());
    }
  }

  private void sendData(Object data, HttpURLConnection connection) throws ClientException {
    try {
      connection.setDoOutput(true);
      connection.addRequestProperty("Content-Type", "application/json");
      var os=connection.getOutputStream();
      byte[] input=new Gson().toJson(data).getBytes(StandardCharsets.UTF_8);
      os.write(input, 0, input.length);
    } catch (IOException ex) {
      throw new ClientException(400, ex.getMessage());
    }
  }

  private <T> T readData(HttpURLConnection connection, Class<T> classOfT) throws ClientException {
    try {
      InputStream responseBody;
      try {
        responseBody=connection.getInputStream();
      } catch (IOException ignored) {
        responseBody=connection.getErrorStream();
      }
      var reader=new BufferedReader(new InputStreamReader(responseBody));
      var response=new StringBuilder();
      String line;

      while ((line=reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      responseBody.close();

      return new Gson().fromJson(response.toString(), classOfT);
    } catch (IOException ex) {
      throw new ClientException(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
    }
  }

  private record ErrorResponse(String message) {
  }

  private record GamesList(ArrayList<GameInfo> games) {
  }

  private record JoinGameRequest(int gameID, String playerColor) {
  }
}

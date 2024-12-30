package ui;

import chess.*;
import models.AuthData;
import serverfacade.NotificationHandler;
import serverfacade.ServerFacade;
import serverfacade.WebsocketFacade;
import ui.clients.*;

import java.util.*;

import static ui.EscapeSequences.*;

public class ChessClient {
  private final ServerFacade server;
  private WebsocketFacade ws;
  private State state;
  private AuthData authData;
  private ChessGame currentGame;
  private ChessGame.TeamColor teamColor;
  private final NotificationHandler notificationHandler;
  private int gameID;

  private Evaluator evaluator;

  public ChessClient(NotificationHandler notificationHandler) {
    server=new ServerFacade();
    state=State.SIGNED_OUT;
    this.notificationHandler = notificationHandler;

    this.evaluator = new SignedOutEvaluator(this);
  }

  public void setAuthData(AuthData authData) {
    this.authData = authData;
  }

  public AuthData getAuthData() {
    return authData;
  }

  public void setGameID(int gameID) {
    this.gameID = gameID;
  }

  public void setTeamColor(ChessGame.TeamColor teamColor) {
    this.teamColor = teamColor;
  }

  public ChessGame.TeamColor getTeamColor() {
    return teamColor;
  }

  public void setCurrentGame(ChessGame game) {
    currentGame = game;
  }

  public ChessGame getCurrentGame() {
    return currentGame;
  }

  public void setState(State state) {
    this.state = state;

    this.evaluator = switch (state) {
        case SIGNED_OUT -> new SignedOutEvaluator(this);
        case SIGNED_IN -> new SignedInEvaluator(this);
        case IN_GAME -> new InGameEvaluator(this);
        case OBSERVING -> new ObservingEvaluator(this);
    };
  }

  public ServerFacade getServer() {
    return server;
  }

  public WebsocketFacade getWs() {
    return ws;
  }

  public void openWs() throws ClientException {
    ws = new WebsocketFacade(server.getURL().toString(), notificationHandler, authData, gameID, this);
  }

  public void closeWs() {
    this.ws = null;
  }

  public State getState() {
    return state;
  }

  public String eval(String input) throws ClientException {
    var inputs=input.split("\\s+");

    if (inputs[0].isEmpty()) {
      return "";
    }

    var params=Arrays.copyOfRange(inputs, 1, inputs.length);

    String output = switch (inputs[0]) {
      case "help" -> evaluator.help();
      case "info" -> info();
      default -> null;
    };

    if (output == null) {
      output = evaluator.eval(inputs[0], params);
    }

    if (output == null) {
      output = "Unknown command. Type 'help' to see all commands.";
    }

    return output;
  }

  private String info() {
    return "info";
  }

  public String displayGame(ChessGame game) {
    this.currentGame = game;
    return this.displayBoard(game.getBoard(), this.teamColor);
  }

  public String displayGame(ChessGame game, ChessPosition pieceMovesToHighlight) {
    this.currentGame = game;
    return displayBoard(game.getBoard(), teamColor, pieceMovesToHighlight);
  }

  private String displayBoard(ChessBoard board, ChessGame.TeamColor perspective) {
    return displayBoard(board, perspective, null);
  }

  private String displayBoard(ChessBoard board, ChessGame.TeamColor perspective, ChessPosition pieceMovesToHighlight) {
    var order=new int[]{0, 1, 2, 3, 4, 5, 6, 7};

    if (perspective != ChessGame.TeamColor.BLACK) {
      for (int index=0; index < order.length / 2; ++index) {
        var temp=order[index];
        order[index]=order[7 - index];
        order[7 - index]=temp;
      }
    }

    var tempGame=new ChessGame();
    tempGame.setBoard(board);
    Collection<ChessMove> moves=new HashSet<>();
    if (pieceMovesToHighlight != null) {
      moves=tempGame.validMoves(pieceMovesToHighlight);
    }

    var highlightedPos=new HashSet<ChessPosition>();
    highlightedPos.add(pieceMovesToHighlight);

    for (var move : moves) {
      highlightedPos.add(move.getEndPosition());
    }

    var sb=new StringBuilder();

    for (int y=0; y < 10; ++y) {
      for (int x=0; x < 10; ++x) {
        renderPos(x, y, sb, perspective, highlightedPos, order, board);
      }
      sb.append(RESET).append("\n");
    }
    return sb.toString();
  }

  private void renderPos(
    int x,
    int y,
    StringBuilder sb,
    ChessGame.TeamColor perspective,
    Set<ChessPosition> highlightedPos,
    int[] order,
    ChessBoard board
  ) {
    var horizontalChars=new char[]{'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'};
    var verticalChars=new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};

    String background;
    String textColor=SET_TEXT_COLOR_GREEN;
    char charToPlace=' ';
    if (x == 0 || x == 9 || y == 0 || y == 9) {
      background=SET_BG_COLOR_LIGHT_GREY;
      textColor=SET_TEXT_COLOR_BLACK;
      if (y == 0 || y == 9) {
        if (x != 0 && x != 9) {
          charToPlace=horizontalChars[order[x - 1]];
        }
      } else {
        charToPlace=verticalChars[order[y - 1]];
      }
    } else {
      if (y % 2 == 0) {
        if (x % 2 == 1) {
          background=SET_BG_COLOR_BLACK;
        }
        else {
          background=SET_BG_COLOR_WHITE;
        }
      } else {
        if (x % 2 == 0) {
          background=SET_BG_COLOR_BLACK;
        }
        else {
          background=SET_BG_COLOR_WHITE;
        }
      }

      ChessPosition pos;

      if (perspective != ChessGame.TeamColor.BLACK) {
        pos=new ChessPosition(8 - (y - 1), x);
      }
      else {
        pos=new ChessPosition(y, 8- (x - 1));
      }

      if (highlightedPos.contains(pos)) {
        if (background.equals(SET_BG_COLOR_BLACK)) {
          background=SET_BG_COLOR_DARK_GREEN;
        }
        else {
          background=SET_BG_COLOR_GREEN;
        }
      }

      var piece=board.getPiece(pos);
      if (piece != null) {
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
          textColor=SET_TEXT_COLOR_BLUE;
        }
        else {
          textColor=SET_TEXT_COLOR_RED;
        }

        charToPlace=piece.getPieceChar();
      }
    }

    sb.append(background).append(textColor);
    sb.append(" ").append(charToPlace).append(" ");
  }

  public String help() {
    return evaluator.help();
  }
}

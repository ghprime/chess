package ui;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
  private final ChessClient client;

  public Repl() {
    this.client=new ChessClient();
  }

  public void run() {
    System.out.println("Welcome to chess! Log in to start.");
    System.out.println(client.help());

    var scanner=new Scanner(System.in);
    var result="";
    while (!result.equals("quit")) {
      printPrompt();
      String line=scanner.nextLine();

      if (line.isEmpty()) {
        line="clear;register user pass email;create game;list;join 1 WHITE;redraw";
      }

      var inputs=line.split(";");

      for (var input : inputs) {
        try {
          result=client.eval(input);
          System.out.println(SET_TEXT_COLOR_LIGHT_GREY + result);
        } catch (Exception e) {
          System.out.println(SET_TEXT_COLOR_RED + e.getMessage());
        }
      }
    }
  }

  private void printPrompt() {
    var color=switch (client.getState()) {
      case SIGNED_OUT -> SET_TEXT_COLOR_RED;
      default -> SET_TEXT_COLOR_GREEN;
    };
    var status=switch (client.getState()) {
      case SIGNED_OUT -> "LOGGED OUT";
      case SIGNED_IN -> "LOGGED IN";
      case IN_GAME -> "IN GAME";
      case OBSERVING -> "OBSERVING";
    };
    var fullStatus=color + "[" + status + "]";
    System.out.print("\n" + RESET + fullStatus + RESET + " >>> " + SET_TEXT_COLOR_BLUE);
  }
}

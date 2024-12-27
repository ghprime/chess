package service.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.daointerface.GameDAO;
import dataaccess.exception.BadRequestException;
import dataaccess.exception.DataAccessException;
import models.AuthData;
import models.GameData;
import server.websocket.ConnectionManager;
import websocket.commands.MakeMoveCommand;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Collection;

public class MakeMoveService {
    private final ConnectionManager conns;
    private final GameDAO gameDAO;

    public MakeMoveService(ConnectionManager conns, GameDAO gameDAO) {
        this.conns = conns;
        this.gameDAO = gameDAO;
    }

    public void makeMove(MakeMoveCommand command) throws DataAccessException {
        AuthData authData = conns.validateAuthToken(command.getAuthToken());

        GameData game = gameDAO.getGameData(command.getGameID());

        ChessGame chessGame = game.game();

        ChessMove move = command.getMove();

        String color = validateMove(authData.username(), game, move);

        try {
            chessGame.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new DataAccessException(e.getMessage());
        }

        boolean gameOver = chessGame.isInCheckmate(chessGame.getTeamTurn())
                || chessGame.isInStalemate(chessGame.getTeamTurn());

        GameData updatedGame = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                chessGame,
                gameOver
        );

        gameDAO.updateGameData(updatedGame);

        LoadGameMessage message = new LoadGameMessage(chessGame);

        conns.broadcastMessage(command, message);
        conns.broadcastMessage(command, new NotificationMessage(
                String.format(
                        "%s (%s) has moved %s",
                        authData.username(),
                        color,
                        move
                )
        ));
        conns.sendMessage(command, message);

        sendChessMessage(authData.username(), game, gameOver, command);
    }

    private void sendChessMessage(String username, GameData game, boolean gameOver, MakeMoveCommand command) throws DataAccessException {
        ChessGame chessGame = game.game();

        String otherUser;
        String otherColor;
        if (username.equals(game.whiteUsername())) {
            otherUser = game.blackUsername();
            otherColor = "BLACK";
        } else {
            otherUser = game.whiteUsername();
            otherColor = "WHITE";
        }

        NotificationMessage notificationMessage;
        if (gameOver) {
            if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
                notificationMessage = new NotificationMessage(
                        String.format(
                                "%s (%s) is in checkmate! Game over!",
                                otherUser,
                                otherColor
                        )
                );
            } else {
                notificationMessage = new NotificationMessage(
                        String.format(
                                "%s (%s) is in stalemate! Game over!",
                                otherUser,
                                otherColor
                        )
                );
            }

        } else if (chessGame.isInCheck(chessGame.getTeamTurn())) {
            notificationMessage = new NotificationMessage(
                    String.format(
                            "%s (%s) is in check",
                            otherUser,
                            otherColor
                    )
            );
        } else {
            return;
        }

        conns.broadcastMessage(command, notificationMessage);
        conns.sendMessage(command, notificationMessage);
    }

    private String validateMove(String username, GameData game, ChessMove move) throws DataAccessException {
        if (move == null) {
            throw new BadRequestException();
        }

        ChessGame.TeamColor color;

        ChessGame chessGame = game.game();

        if (username.equals(game.whiteUsername())) {
            color = ChessGame.TeamColor.WHITE;
        } else if (username.equals(game.blackUsername())) {
            color = ChessGame.TeamColor.BLACK;
        } else {
            throw new DataAccessException("Cannot make moves as an observer!");
        }

        if (game.gameOver()) {
            throw new DataAccessException("Cannot make moves after the game is over!");
        }

        if (chessGame.getTeamTurn() != color) {
            throw new DataAccessException("Not your turn!");
        }

        if (chessGame.getBoard().getPiece(move.getStartPosition()).getTeamColor() != color) {
            throw new DataAccessException("Not your piece!");
        }

        Collection<ChessMove> moves = chessGame.validMoves(move.getStartPosition());

        if (!moves.contains(move)) {
            throw new DataAccessException("Invalid move!");
        }

        return color == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK";
    }
}

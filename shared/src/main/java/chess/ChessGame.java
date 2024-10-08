package chess;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor currentTeamTurn=TeamColor.WHITE;
    ChessBoard board=new ChessBoard();

    public static ChessGame deserialize(String serializedGame, TeamColor currentTeamTurn) {
        var game=new ChessGame();
        game.setTeamTurn(currentTeamTurn);
        game.setBoard(ChessBoard.deserialize(serializedGame));
        return game;
    }

    public ChessGame() {
        board.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeamTurn=team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        var piece=board.getPiece(startPosition);
        if (piece == null) {
            return Collections.emptyList();
        }

        return piece.pieceMoves(board, startPosition).stream().filter(move -> !moveCausesCheck(move)).collect(Collectors.toSet());
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var pieceToMove=board.getPiece(move.getStartPosition());

        if (pieceToMove == null) {
            throw new InvalidMoveException("Not a valid piece to move!");
        }

        if (pieceToMove.getTeamColor() != currentTeamTurn) {
            throw new InvalidMoveException("Not correct team color!");
        }

        var placeToMove=board.getPiece(move.getEndPosition());

        if (placeToMove != null && pieceToMove.getTeamColor() == placeToMove.getTeamColor()) {
            throw new InvalidMoveException("Cannot capture friendly pieces!");
        }

        var allMoves=pieceToMove.pieceMoves(board, move.getStartPosition());

        if (!allMoves.contains(move)) {
            throw new InvalidMoveException("Not a valid move!");
        }

        board.addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() == null) {
            board.addPiece(move.getEndPosition(), pieceToMove);
        }

        else {
            board.addPiece(move.getEndPosition(), new ChessPiece(pieceToMove.getTeamColor(), move.getPromotionPiece()));
        }

        if (isInCheck(currentTeamTurn)) {
            if (placeToMove == null) {
                board.addPiece(move.getEndPosition(), null);
            }
            else {
                board.addPiece(move.getEndPosition(), new ChessPiece(pieceToMove.getTeamColor(), placeToMove.getPieceType()));
            }
            board.addPiece(move.getStartPosition(), new ChessPiece(pieceToMove.getTeamColor(), pieceToMove.getPieceType()));
            throw new InvalidMoveException("Move would result in check!");
        }
        setTeamTurn(currentTeamTurn == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        var moves=new HashSet<ChessPosition>();
        ChessPosition kingPos=new ChessPosition(-1, -1);
        for (int row=1; row < 9; ++row) {
            for (int col=1; col < 9; ++col) {
                var pos=new ChessPosition(row, col);
                var piece=board.getPiece(pos);
                if (piece == null) {
                    continue;
                }
                if (piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos=pos;
                }
                if (piece.getTeamColor() == teamColor) {
                    continue;
                }
                for (var move : piece.pieceMoves(board, pos)) {
                    moves.add(move.getEndPosition());
                }
            }
        }

        return kingPos.getColumn() != -1 && moves.contains(kingPos);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        var blackMoves=new HashSet<ChessMove>();
        var whiteMoves=new HashSet<ChessMove>();
        getAllMoves(blackMoves, whiteMoves);
        var moves=teamColor == TeamColor.BLACK ? blackMoves : whiteMoves;
        for (var move : moves) {
            if (!moveCausesCheck(move)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        var kingsMoves=new HashSet<ChessMove>();
        var enemyMoves=new HashSet<ChessMove>();
        for (int row=1; row < 9; ++row) {
            for (int col=1; col < 9; ++col) {
                var pos=new ChessPosition(row, col);
                var piece=board.getPiece(pos);
                if (piece == null) {
                    continue;
                }
                if (piece.getTeamColor() == teamColor) {
                    if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingsMoves.addAll(piece.pieceMoves(board, pos));
                    }
                    else if (!piece.pieceMoves(board, pos).isEmpty()) {
                        return false;
                    }
                } else {
                    enemyMoves.addAll(piece.pieceMoves(board, pos));
                }
            }
        }
        return kingsMoves.stream().filter(enemyMoves::contains).collect(Collectors.toSet()).isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board=board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private void getAllMoves(Set<ChessMove> blackMoves, Set<ChessMove> whiteMoves) {
        for (int row=1; row < 9; ++row) {
            for (int col=1; col < 9; ++col) {
                var pos=new ChessPosition(row, col);
                var piece=board.getPiece(pos);
                if (piece == null) {
                    continue;
                }
                if (piece.getTeamColor() == TeamColor.BLACK) {
                    blackMoves.addAll(piece.pieceMoves(board, pos));
                }
                else {
                    whiteMoves.addAll(piece.pieceMoves(board, pos));
                }
            }
        }
    }

    private boolean moveCausesCheck(ChessMove move) {
        var pieceToMove=board.getPiece(move.getStartPosition());
        var pieceAtEndPos=board.getPiece(move.getEndPosition());

        var startPos=move.getStartPosition();
        var endPos=move.getEndPosition();

        var teamColor=pieceToMove.getTeamColor();
        var newPieceType=move.getPromotionPiece() != null ? move.getPromotionPiece() : pieceToMove.getPieceType();
        var newPiece=new ChessPiece(teamColor, newPieceType);

        board.addPiece(startPos, null);
        board.addPiece(endPos, newPiece);

        boolean causesCheck=isInCheck(teamColor);

        board.addPiece(startPos, pieceToMove);
        board.addPiece(endPos, pieceAtEndPos);

        return causesCheck;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        var otherGame=(ChessGame) obj;
        return board.equals(otherGame.board);
    }

    @Override
    public String toString() {
        return board.toString();
    }

    public String serialize() {
        return board.serialize();
    }
}

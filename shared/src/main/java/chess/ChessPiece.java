package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color=pieceColor;
        this.type=type;
    }

    public static ChessPiece deserialize(char piece) {
        return switch (piece) {
            case 'r' -> new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.ROOK);
            case 'n' -> new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.KNIGHT);
            case 'b' -> new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.BISHOP);
            case 'k' -> new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.KING);
            case 'q' -> new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.QUEEN);
            case 'p' -> new ChessPiece(ChessGame.TeamColor.BLACK, PieceType.PAWN);
            case 'R' -> new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.ROOK);
            case 'N' -> new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.KNIGHT);
            case 'B' -> new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.BISHOP);
            case 'K' -> new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.KING);
            case 'Q' -> new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.QUEEN);
            case 'P' -> new ChessPiece(ChessGame.TeamColor.WHITE, PieceType.PAWN);
            default -> null;
        };
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return MoveCalculator.getMoves(board, myPosition, board.getPiece(myPosition));
    }

    public char getPieceChar() {
        return switch (type) {
            case ROOK -> 'R';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case KING -> 'K';
            case QUEEN -> 'Q';
            case PAWN -> 'P';
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        var otherPiece=(ChessPiece) obj;
        return otherPiece.getPieceType() == type && otherPiece.getTeamColor() == color;
    }

    @Override
    public String toString() {
        int offset=color == ChessGame.TeamColor.BLACK ? 0 : 32;
        char letter='e';

        switch (type) {
            case ROOK -> letter='r';
            case KNIGHT -> letter='n';
            case BISHOP -> letter='b';
            case KING -> letter='k';
            case QUEEN -> letter='q';
            case PAWN -> letter='p';
        }

        return String.valueOf((char) (letter - offset));
    }
}

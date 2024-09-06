package chess;

import java.util.function.BiFunction;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] board=new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */

    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[8 - position.getRow()][position.getColumn() - 1]=piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[8 - position.getRow()][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // PieceFactory
        BiFunction<ChessGame.TeamColor, ChessPiece.PieceType, ChessPiece> pf=
                ChessPiece::new;
        var white=ChessGame.TeamColor.WHITE;
        var black=ChessGame.TeamColor.BLACK;
        var rook=ChessPiece.PieceType.ROOK;
        var knight=ChessPiece.PieceType.KNIGHT;
        var bishop=ChessPiece.PieceType.BISHOP;
        var king=ChessPiece.PieceType.KING;
        var queen=ChessPiece.PieceType.QUEEN;
        var pawn=ChessPiece.PieceType.PAWN;

        board=new ChessPiece[][]{
                {pf.apply(black, rook), pf.apply(black, knight), pf.apply(black, bishop), pf.apply(black, queen), pf.apply(black, king), pf.apply(black, bishop), pf.apply(black, knight), pf.apply(black, rook)},
                {pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn), pf.apply(black, pawn)},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn), pf.apply(white, pawn)},
                {pf.apply(white, rook), pf.apply(white, knight), pf.apply(white, bishop), pf.apply(white, queen), pf.apply(white, king), pf.apply(white, bishop), pf.apply(white, knight), pf.apply(white, rook)},
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        var otherBoard=(ChessBoard) obj;
        for (int row=0; row < 8; ++row) {
            for (int col=0; col < 8; ++col) {
                var piece=board[row][col];
                var otherPiece=otherBoard.board[row][col];
                if (piece == null && otherPiece == null) continue;
                if (piece == null) return false;
                if (!piece.equals(otherPiece)) return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        var sb=new StringBuilder().append('|');
        for (int row=0; row < 8; ++row) {
            for (int col=0; col < 8; ++col) {
                var piece=board[row][col];
                sb.append(piece == null ? "." : piece).append('|');
            }
            if (row != 7) sb.append("\n|");
        }
        return sb.toString();
    }

    public static ChessBoard deserialize(String serializedGame) {
        ChessBoard board=new ChessBoard();
        for (int x=0; x < 8; ++x) {
            for (int y=0; y < 8; ++y) {
                var piece=ChessPiece.deserialize(serializedGame.charAt(x * 8 + y));
                board.addPiece(new ChessPosition(x, y), piece);
            }
        }
        return board;
    }

    public String serialize() {
        var sb=new StringBuilder();
        for (int row=0; row < 8; ++row) {
            for (int col=0; col < 8; ++col) {
                var piece=board[row][col];
                sb.append(piece == null ? "." : piece);
            }
        }
        return sb.toString();
    }
}

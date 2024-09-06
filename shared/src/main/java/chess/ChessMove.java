package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {
    final private ChessPosition startPos;
    final private ChessPosition endPos;
    final private ChessPiece.PieceType promotionType;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPos = startPosition;
        this.endPos = endPosition;
        this.promotionType = promotionPiece;
    }

    public ChessMove(ChessPosition startPos, ChessPosition endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.promotionType = null;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return this.startPos;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return this.endPos;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return this.promotionType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        var otherMove=(ChessMove) obj;
        if (otherMove.promotionType != promotionType) {
            return false;
        }
        return startPos.equals(otherMove.startPos) && endPos.equals(otherMove.endPos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPos, endPos, promotionType);
    }

    @Override
    public String toString() {
        var sb=new StringBuilder();
        sb.append(startPos).append("->").append(endPos);
        if (promotionType != null) {
            sb.append(" (").append(promotionType).append(')');
        }
        return sb.toString();
    }
}

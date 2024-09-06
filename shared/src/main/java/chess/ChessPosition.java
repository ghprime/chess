package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private static final char[] FILE_NAMES=new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
    private static final char[] RANK_NAMES=new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};
    private final int row;
    private final int column;

    public ChessPosition(int row, int col) {
        this.column=col;
        this.row=row;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        var otherPos=(ChessPosition) obj;
        return row == otherPos.row && column == otherPos.column;
    }

    @Override
    public int hashCode() {
        return FILE_NAMES[column - 1] + (RANK_NAMES[row - 1] >> 4);
    }

    @Override
    public String toString() {
        return "(" + FILE_NAMES[column - 1] + ',' + RANK_NAMES[row - 1] + ')';
    }
}

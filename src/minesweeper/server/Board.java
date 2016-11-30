package minesweeper.server;

/**
 * Board represents a thread safe mutable grid of characters in multi-player game 'Minesweeper'.
 * @author win8
 *
 */
public interface Board {
    /**
     * 
     * @return content of this Board as single string.
     */
    public String getBoardState();
    
    
    /**
     * Modifies cell state by replacing current state with 'new state'.
     * @param state new cell state.
     * @param posX cell position in columns >= 0 and <= Board X size
     * @param posY cell position in rows >= 0 and <= Board Y size
     * @return true if current cell state is changed to new state, otherwise false.
     */
    public boolean changeCellState(int posX, int posY, char state);
    
    
    /**
     * Getting current Board cell state
     * @param posX cell position in columns >=0 and <= Board X size
     * @param posY cell position in rows >=0 and <= Board Y size
     * @return character witch represent valid cell state
     */
    public char getCellState(int posX, int posY);
    
    
    /**
     * @return Board dimensions represented by columns-X size and rows-Y size
     */
    public int[] getBoardSize();
    
}

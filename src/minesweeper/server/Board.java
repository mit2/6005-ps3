package minesweeper.server;

/**
 * Board represents a thread safe mutable grid of characters in multi-player game 'Minesweeper'.
 * @author win8
 *
 */
public interface Board {
    /**
     * 
     * @return content of this Board.
     */
    public String getBoardState();
    
    /**
     * Modifies cell state by replacing current state with 'new state'.
     * @param state new cell state.
     * @return true if current cell state is changed to new state, otherwise false.
     */
    public boolean changeCellState(char state);

}

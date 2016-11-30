package minesweeper.server;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;

/**
 * Ã Board represents a thread safe mutable 2D Array of character set {_, ,F,1} in multiplayer game Minesweeper.
 * @author win8
 *
 */
public class SimpleBoard implements Board{
    private final int numRows, numCols, numBombs;
    private final Character [][] board;
    private final Character [] validCellState = {'_',' ','F','B','1','2','3','4','5','6','7','8'};  // check size: cellStates.length, soon will be sorted for rep-invariant checking purposes
                                                                                                    // remove 'B' from set after debugging!
    //private final List<Character> validCellState = new ArrayList<Character>(Arrays.asList(cellStates)); // not used, as list will not resized
    
    // Rep invariant
    // board numBoms >= 0 && numBoms <= initSize
    // board numRows > 0 && numRows == initSize
    // board numCols > 0 && numCols == initSize
    // each Board cell is not empty and contain only chars from Valid State Character Set {_, ,F,B,1-8}
    
    // Abstraction function
    // represents a grid of characters from set of valid characters.
    
    // Thread safety argument:
    // --------------------------------------------------
    //    This class is threadsafe because it has:
    //    Thread safety argument with immutability:
    //    - numRows, numCols, numBombs, board, validCellState are final
    //    - 'board' points to a mutable char array, but that array is encapsulated
    //      in this object, not shared with any other object or exposed to a client
    //      
    //    - 'validCellState' points to a mutable char array, but that array is encapsulated
    //      in this object, not shared with any other object or exposed to a client
    //
    //    Thread safety argument with synchronization:  
    //    - all accesses to 'board' happen within SimpleBoard methods,
    //      which are all guarded by SimpleBoard'S lock
    
    
    /**
     * Construct initial Board state.
     * @param cols number of Board columns
     * @param rows number of Board rows
     * @param bombs number of Bombs to be inserted
     */
    public SimpleBoard(int rows, int cols, int bombs){
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS! invalid rows", rows > 0); // validate board rows size
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS! invalid cols", cols > 0); //
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS! invalid bombs", bombs >= 0);
        
        numBombs = bombs;
        numRows = rows;
        numCols = cols;
        board = new Character[rows][cols];
      
        // Fill-up the Board cells with 'untouched' state; insert 'initSize' bombs in random cell locations.
        int insertBombs = 0; // was needed for manual insertion.
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {                                 
                if(randomBombInsert() && (numBombs == Integer.MAX_VALUE)) { // sentinel: random bomb insertion with no manual limit, you should assign each square to contain a bomb with probability .25 and otherwise no bomb.
                    board[i][j] = validCellState[3];                    
                }
                else board[i][j] = validCellState[0];               
            }
        }

        //Arrays class contains various methods for manipulating arrays (such as sorting and searching).
        Arrays.sort(validCellState); // sort validCellState array into ascending order for checkRep() checks

        checkRep(); // validate Board ADT
    }

    /**
     * Using randomness to insert bomb. 
     * @return a true if value less than or equal to 0.25
     */
    private boolean randomBombInsert(){
        return Math.random() <= 0.25;
    }
    
    /**
     *  Checking Rep Invariant
     *  "You should certainly call checkRep() to assert the rep invariant at the end of every operation that
     *  creates or mutates the rep – in other words, creators, producers, and mutators."
     *  What to Assert: Pre-post conditions, rep invariants, loop invariants, covering all cases: 'switch' assertion in default case
     *  Lec-5
     *  Check that the rep invariant is true.
     */
    private void checkRep(){
        int countBombs = 0;
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS!", board.length > 0 && board.length == numRows); // validate board rows size
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS!", board[0].length > 0 && board[0].length == numCols); // validate board cols size
             
        // traverse thru all board and check if each cell contain only validCellState
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(Arrays.binarySearch(validCellState, board[i][j]) < 0) throw new AssertionError(); // WORNING: validCellState is sorted!!!               
                if(board[i][j] == 'B') countBombs++; // count bombs                
            }
        }

        Assert.assertTrue("ASSERTION ERROR ON COUNTBOMBS!", (countBombs >= 0) && (countBombs <= numBombs)); // validate board bombs size        
    }
    
    @Override
    public synchronized  String getBoardState() {
        // Best way is print cell state to std_out stream... will do later
        String boardContent = "";
        Assert.assertTrue("ASSERTION ERROR ON 'BOARD' REP INVARIANT!", board.length > 0);
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(board[i][j] == 'B') boardContent = boardContent.concat("_") + " "; // mask Bombs location for output
                else boardContent = boardContent.concat(board[i][j].toString()) + " ";
            }
            boardContent = boardContent.substring(0, boardContent.length()-1);  // Eliminate last in the line empty space
            boardContent = boardContent + "\r\n";
        }
        return boardContent;
    }

    @Override
    public  synchronized boolean changeCellState(int posX, int posY, char state) {
        // Any invalid position will fail change
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS!", posX >= 0 || posX <= numCols-1); // validate cell x pos
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS!", posY >= 0 || posY <= numRows-1);
                
        char oldState = board[posY][posX];
        char newState = state;
        
        // Any invalid state will fail change
        if((Arrays.binarySearch(validCellState, newState) < 0) || (oldState == newState)) return false;                                 
        else board[posY][posX] = newState;
                    
        checkRep();
       
        Assert.assertEquals(Character.toString(newState), board[posY][posX].toString());// Asserting post-condition        
        return  board[posY][posX] == newState;
    }

    @Override
    public  synchronized char getCellState(int posX, int posY) {
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS!", posX >= 0 || posX <= numCols-1); // validate cell x pos
        Assert.assertTrue("ASSERTION ERROR ON INPUT PARAMS!", posY >= 0 || posY <= numRows-1);
        
        Assert.assertTrue(Arrays.binarySearch(validCellState, board[posY][posX]) >= 0); // Asserting post-condition
        return board[posY][posX];
    }

    
    @Override
    public int[] getBoardSize() {
        int [] boardDim = new int[2];
        boardDim[0] = numCols;
        boardDim[1] = numRows;
        
        return boardDim;
    }
}

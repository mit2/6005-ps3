package minesweeper.server;

import java.util.Arrays;

import org.junit.Assert;

/**
 * Ã Board represents a thread safe mutable 2D Array of character set {_, ,f,b,0-9} in multiplayer game Minesweeper.
 * @author win8
 *
 */
public class SimpleBoard implements Board{
    private final int numRows, numCols, numBombs;
    private final Character [][] board;
    private final Character [] validCellState = {'_',' ','F','B','1','2','3','4','5','6','7','8'};  // check size 12: cellStates.length
    //private final List<Character> validCellState = new ArrayList<Character>(Arrays.asList(cellStates));
    
    // Rep invariant
    // board numBoms > 0 && numBoms == initSize
    // board numRows > 0 && numRows == initSize
    // board numCols > 0 && numCols == initSize
    // each Board cell is not empty and contain only chars from Valid State Character Set {_, ,f,b,1-8}
    
    // Abstraction function
    // represents a grid of characters from set of valid characters.
    
    // Thread safety argument
    // ...
    // ...
    
    
    /**
     * Construct initial Board state.
     * @param cols number of Board columns
     * @param rows number of Board rows
     */
    public SimpleBoard(int rows, int cols, int bombs){
        numBombs = bombs;
        numRows = rows;
        numCols = cols;
        board = new Character[rows][cols];
        
        // Fill-up the Board cells with 'untouched' state; insert 'initSize' bombs in random cell locations.
        int insertBombs = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if(randomBombInsert() && (insertBombs < numBombs)){ //  you should assign each square to contain a bomb with probability .25 and otherwise no bomb.
                    board[i][j] = 'B'; 
                    insertBombs++;                    
                }
                else board[i][j] = '_';               
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
                if(Arrays.binarySearch(validCellState, board[i][j]) < 0) throw new AssertionError();                
                if(board[i][j] == 'B') countBombs++; // count bombs
            }
        }
        
        Assert.assertTrue("ASSERTION ERROR ON COUNTBOMBS!", countBombs == numBombs); // validate board bombs size        
    }
    
    @Override
    public String getBoardState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean changeCellState(char state) {
        // TODO Auto-generated method stub
        return false;
    }
    
    

}

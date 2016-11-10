package minesweeper.server;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleBoardTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    
    
    
    // getBoardState()
    @Test
    public void testGetBoardState_Empty() {
        //Board b = new SimpleBoard(0, 0, 0); // it is impossible by Board design to be empty.
    }
    
    @Test
    /**
     * Testing if Board contain only valid cell states from ValidStateCharSet
     */
    public void testGetBoardState_ValidStateCharSet() {
        Board b = new SimpleBoard(3, 3, 0);
        //System.out.println("PASSED");
        b.changeCellState(0, 0, ' '); // add DUG cell state
        b.changeCellState(0, 1, 'F'); // add FLAGGED cell state
        b.changeCellState(2, 1, 'F'); // add FLAGGED cell state
        
        System.out.println(b.getBoardState());
        String checkString =   "  _ _\n"
                             + "F _ F\n"
                             + "_ _ _\n";
        assertEquals("String Not equals!", checkString, b.getBoardState());
    }
    
    @Test
    public void testGetBoardState_InvalidStateCharSet() {
        // Impossible by Board design implementation to have invalid cell states
    }
    
    @Test
    public void testGetBoardState_MixedStateCharSet() {
     // Impossible by Board design implementation to have invalid cell states
    }
    
    
    
    // changeCellState()
    @Test
    public void testChangeCellState_Empty() {
        Board b = new SimpleBoard(10, 10, 4);        
        assertFalse(b.changeCellState(0, 0, '\0'));   // as 'empty' or any invalid state     
    }
    
    @Test
    // Server doing changes in a cell
    public void testChangeCellState_ValidStateCharSet() {
        Character [] validCellState = {'_',' ','F','B'};    // size 12
        Board b = new SimpleBoard(10, 10, 4);
        
        for (int i = 0; i < validCellState.length; i++) {
            if(i == 3) continue; // skip inserting bomb state to not violate rep invariant by overcount
            if(i == 0) assertFalse(b.changeCellState(0, 0, validCellState[i])); // change cell state to new same state not permitted.
            else assertTrue(b.changeCellState(0, 0, validCellState[i])); // changing state of one cell
        }           
    }
    
    @Test
    public void testChangeCellState_InvalidStateCharSet() {
        Character [] invalidCellState = {'a','b','c', '@', ';'};
        Board b = new SimpleBoard(10, 10, 4);
        for (int i = 0; i < invalidCellState.length; i++) {
            assertFalse(b.changeCellState(0, 0, '\0'));   // to any invalid state 
        }
         
    }
    
    @Test
    public void testChangeCellState_InvalidCellPosition() {
        Board b = new SimpleBoard(10, 10, 4);
        assertFalse(b.changeCellState(11, 11, 'F')); // out of Board above limit
        assertFalse(b.changeCellState(-1, -1, 'F')); // out of Board bellow limit
        assertFalse(b.changeCellState(-1, 1, 'F')); 
        
    }
    
    
    
    // getCellState()
    @Test
    public void testGetCellState_Empty() {
        //Board b = new SimpleBoard(0, 0, 0); // Impossible by Board design implementation to have invalid cell states                    
    }
    
    @Test
    public void testGetCellState_ValidState() {
        Board b = new SimpleBoard(10, 10, 4);
        b.changeCellState(0, 0, 'F');
        Character ch = b.getCellState(0, 0);
        assertEquals("F", ch.toString());
    }
    
    @Test
    public void testGetCellState_InvalidState() {
        Character [] invalidCellState = {'a','b','c', '@', ';'}; // Impossible by Board design implementation to have invalid cell states                    
    }

}

package minesweeper.server;

import static org.junit.Assert.*;

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

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    
    
    // getBoardState()
    @Test
    public void testGetBoardState_Empty() {
        Board b = new SimpleBoard(0, 0, 0);
        // Will be called AssertionError by checkRep()
        
    }
    
    @Test
    public void testGetBoardState_ValidStateCharSet() {
        Board b = new SimpleBoard(10, 10, 4);
        System.out.println("PASSED");
    }
    
    @Test
    public void testGetBoardState_InvalidStateCharSet() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testGetBoardState_MixedStateCharSet() {
        fail("Not yet implemented");
    }
    
    
    
    // getCellState()
    @Test
    public void testGetCellState_Empty() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testGetCellState_ValidStateCharSet() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testGetCellState_InvalidStateCharSet() {
        fail("Not yet implemented");
    }

}

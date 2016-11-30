package minesweeper.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.server.ServerCloneException;

public class MinesweeperServerThread implements Runnable{
    /**
     * MSSThread stays for MinesweeperServerThread.
     */
    private final Thread MSSThread; // reference to current Thread in case i need to manage it.
    private final Socket socket;
    private final SimpleBoard board;
    
    public MinesweeperServerThread(Socket socket, SimpleBoard board){
        this.socket = socket;
        this.board = board;
        MSSThread = new Thread(this);
        MSSThread.start();
        
    }
    
    /**
     * Run a thread
     */
    public void run(){
        try {
            handleConnection(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if connection has an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        // try-with-resources Statement
        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            ) 
        {
            // Hello message from server to user sent only once, immediately after the server connects to the user.          
            out.println("Welcome to Minesweeper. Board: " + board.getBoardSize()[0] + " columns by " + board.getBoardSize()[1] + " rows."
                    + " Players: " + MinesweeperServer.getConnectedPlayers() + " including you. Type 'help' for help.\r\n");
            
            // Handle clients queries durent the Game
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);    // respond to the Client.
                if (output != null) {
                    out.println(output);
                    if(output.equals("BOOM!!!") && !MinesweeperServer.isDebugging()) return; // close connection, server in production mode
                } else return; // action to close connection.                  
            }
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client
     */
    private String handleRequest(String input) {
        String regex = "(look)|(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|"
                + "(deflag -?\\d+ -?\\d+)|(help)|(bye)";
        
        if ( ! input.matches(regex)) {
            // invalid input
            /**
             * For any message from the server which does not match the server-to-user message format as given, do nothing,
             * discard the data until the NEWLINE is reached, and start processing the next message.
             * My Note: it is impossible get wrong message from server by program design, only 'wrong' message can send user.
             */
            //return null; // ORIGINAL ACTION
            return "WRONG COMMAND! Try 'help' to see witch commands is legal to use.\r\n";
            
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            // Should return Board state without revealing Bombs positions!?
            return board.getBoardState();
        } else if (tokens[0].equals("help")) {
            // 'help' request
            // all the commands the user can send to the server.
            return "The commands the user can send to the server: look, dig x y, flag x y, deflag x y, help, bye";
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            // Terminates the connection with this client.
            MinesweeperServer.decreaseNumPlayers();
            return null;
        } else {
            int xPos = Integer.parseInt(tokens[1]); // X cell position
            int yPos = Integer.parseInt(tokens[2]); // y cell position
            if (tokens[0].equals("dig")) {
                // 'dig x y' request              
                if ((xPos >= 0 && yPos >= 0) && (xPos < board.getBoardSize()[0] && yPos < board.getBoardSize()[1])){
                    if(board.getCellState(xPos, yPos) == '_'){                        
                        // Recursively reveal adjacent cells until neighbors with bombs
                        revealCell(xPos, yPos);                        
                        return board.getBoardState();
                    } 
                    else if(board.getCellState(xPos, yPos) == 'B'){
                        board.changeCellState(xPos, yPos, ' ');
                        return "BOOM!!!";
                    } 
                }
               
                return board.getBoardState(); // return unattached board state
              
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request                
                if ((xPos >= 0 && yPos >= 0) && (xPos < board.getBoardSize()[0] && yPos < board.getBoardSize()[1])
                        && board.getCellState(xPos, yPos) == '_')board.changeCellState(xPos, yPos, 'F');
                return board.getBoardState();
                
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request, repeated code from flag section above, usually better to extract into separate call with diff args.                
                if ((xPos >= 0 && yPos >= 0) && (xPos < board.getBoardSize()[0] && yPos < board.getBoardSize()[1])
                        && board.getCellState(xPos, yPos) == 'F')board.changeCellState(xPos, yPos, '_');
                return board.getBoardState();
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
    
    /**
     * Mutate board state by revealing cells with no bombs. (Here implemented Board processing logic)
     * If the square x,y has no neighbor squares with bombs,
       then for each of x,y’s ‘untouched’ neighbor squares, change said square to ‘dug’ and
       repeat this step (not the entire DIG procedure) recursively for said neighbor square 
       unless said neighbor square was already dug before said change.
       else if the square x,y has neighbor squares with bombs, change said square to ‘dug’ and
       update it with number of bombs adjacent to it.
     * @param xPos cell position in columns >=0 and <= Board X size
     * @param yPos cell position in rows >=0 and <= Board Y size
     */
    private void revealCell(int x, int y){
        /**
         * GAME LOGIC
         * The game is played by revealing squares of the grid by clicking or otherwise indicating each square.
         * If a square containing a mine is revealed, the player loses the game. If no mine is revealed,
         * a digit is instead displayed in the square, indicating how many adjacent squares contain mines;
         * if no mines are adjacent, the square becomes blank, and all adjacent squares will be recursively
         * revealed. The player uses this information to deduce the contents of other squares, and may either
         * safely reveal each square or mark the square as containing a mine.
         * 
         * telnet localhost 4444
         * 
         */ 
        
        int count = 0;
        //int[][] revealPos = new int[8][2]; // 8 all possible destination from cell x,y with new (x*,y*) locations.       
        int revealPos[][] = { {x, y-1} , { x+1, y-1} , {x+1, y} , {x+1, y+1}, {x, y+1} , {x-1, y+1} , {x-1, y} , {x-1, y-1} };
        for (int[] pos : revealPos) {
            if((pos[0] >= 0 && pos[0] < board.getBoardSize()[0]) && ((pos[1] >= 0 && pos[1] < board.getBoardSize()[1]))){ // process only valid pos
                if(board.getCellState(pos[0], pos[1]) == 'B') count++;
            }
        }        
        if(count > 0){
            board.changeCellState(x, y,  Character.forDigit(count, 10));
            return; // exit recursion
        } else {
            board.changeCellState(x, y, ' ');
            for (int[] pos : revealPos) {
                if((pos[0] >= 0 && pos[0] < board.getBoardSize()[0]) && ((pos[1] >= 0 && pos[1] < board.getBoardSize()[1]))){ // recurr only on valid pos
                    if(board.getCellState(pos[0], pos[1]) != ' ') revealCell(pos[0], pos[1]); // recurr on relative pos from x,y
                }                
            }  
        }
        
    }
}

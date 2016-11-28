package minesweeper.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);    // respond to the Client.
                if (output != null) {
                    out.println(output);
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
            return "WORNG COMMAND! Try 'help' to see witch commands is legal to use.\r\n";
            
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
            return null;
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                // TODO Question 5
                /**mutate board state, Implement Board processing logic;
                 * If the square x,y has no neighbor squares with bombs,
                   then for each of x,y’s ‘untouched’ neighbor squares, change said square to ‘dug’ and
                   repeat this step (not the entire DIG procedure) recursively for said neighbor square 
                   unless said neighbor square was already dug before said change.*/
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                int xPos = Integer.parseInt(tokens[1]); // X cell position
                int yPos = Integer.parseInt(tokens[2]); // y cell position
                if ((xPos >= 0 && yPos >= 0) && (xPos < board.getBoardSize()[0] && yPos < board.getBoardSize()[1])
                        && board.getCellState(xPos, yPos) == '_')board.changeCellState(xPos, yPos, 'F');
                return board.getBoardState();
                
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request, repeated code from flag section above, usually better to extract into separate call with diff args.
                int xPos = Integer.parseInt(tokens[1]); // X cell position
                int yPos = Integer.parseInt(tokens[2]); // y cell position
                if ((xPos >= 0 && yPos >= 0) && (xPos < board.getBoardSize()[0] && yPos < board.getBoardSize()[1])
                        && board.getCellState(xPos, yPos) == 'F')board.changeCellState(xPos, yPos, '_');
                return board.getBoardState();
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
}

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
    
    public MinesweeperServerThread(Socket socket){
        this.socket = socket;
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
                }
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
            return null;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            // TODO Question 5
        } else if (tokens[0].equals("help")) {
            // 'help' request
            // TODO Question 5
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            // TODO Question 5
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                // TODO Question 5
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                // TODO Question 5
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                // TODO Question 5
            }
        }
        // Should never get here--make sure to return in each of the valid cases above.
        throw new UnsupportedOperationException();
    }
}

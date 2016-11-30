package minesweeper.server;

import java.io.*;
import java.net.*;
import java.util.*;
/** GAME LOGIC AND SYSTEM SAFETY ARGUMENT
* The game is played by revealing squares of the grid by clicking or otherwise indicating each square.
* If a square containing a mine is revealed, the player loses the game. If no mine is revealed,
* a digit is instead displayed in the square, indicating how many adjacent squares contain mines;
* if no mines are adjacent, the square becomes blank, and all adjacent squares will be recursively
* revealed. The player uses this information to deduce the contents of other squares, and may either
* safely reveal each square or mark the square as containing a mine.
* /

/**
 *  // System Thread safety argument
    // (My Note: only analyze top-level objects, not their members)
    // ----------------------
    // The threads in the system are:
    // - main thread accepting new connections
    // - one thread per connected client, handling just that client
    //
    // The serverSocket object is confined to the main thread.
    //
    // The Socket object for a client is confined to that client's thread;
    // the main thread loses its reference to the object right after starting
    // the client thread.
    //
    // The MinesweeperServerThread object for client is confined to that client's thread, so it is thread safe.
    //
    // The Board object is sheared by all client threads, all accesses to 'board' happen within SimpleBoard methods,
    // which are all guarded by SimpleBoard'S lock
    //
    // System.err is used by all threads for displaying error messages.
    //
    // No other shared mutable data.
 */
public class MinesweeperServer {
    /**
     * True is server is listening for connection.
     */
    private final boolean listening = true;
    
    private static int connectedPlayers = 0;
    private final static Object lock = new Object();
    
    private final ServerSocket serverSocket;
    /**
     * True if the server should _not_ disconnect a client after a BOOM message.
     */    
    private  static boolean debug;
    /**
     * MineSweeper game board
     */
    private static SimpleBoard board = null;

    /**
     * Make a MinesweeperServer that listens for connections on port.
     * 
     * @param port port number, requires 0 <= port <= 65535
     */
    public MinesweeperServer(int port, boolean debug) throws IOException {
        serverSocket = new ServerSocket(port);
        MinesweeperServer.debug = debug;
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (listening) {
            // block until a client connects            
            new MinesweeperServerThread(serverSocket.accept(), board);
                // mark down another new connected user;
                connectedPlayers++;
           
        }
    }

    //here was handleConnection() & hadleRequest(); moved to MinesweeperServerThread ADT

    /**
     * Start a MinesweeperServer using the given arguments.
     * 
     * Usage: MinesweeperServer [--debug] [--port PORT] [--size (SIZE_X,SIZE_Y) | --file FILE]
     * 
     * The --debug argument means the server should run in debug mode. The server should disconnect
     * a client after a BOOM message if and only if the debug flag argument was NOT given. E.g.
     * "MinesweeperServer --debug" starts the server in debug mode.
     * 
     * PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the
     * server should be listening on for incoming connections. E.g. "MinesweeperServer --port 1234"
     * starts the server listening on port 1234.
     * 
     * SIZE_X and SIZE_Y are optional integer arguments specifying that a random board of size SIZE_X*SIZE_Y should
     * be generated. E.g. "MinesweeperServer --size 42,69" starts the server initialized with a random
     * board of size 42*69.
     * 
     * FILE is an optional argument specifying a file pathname where a board has been stored. If
     * this argument is given, the stored board should be loaded as the starting board. E.g.
     * "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
     * in boardfile.txt, however large it happens to be (but the board may be assumed to be
     * square).
     * 
     * The board file format, for use with the "--file" option, is specified by the following
     * grammar:
     * 
     *   FILE :== LINE+ 
     *   LINE :== (VAL SPACE)* VAL NEWLINE
     *   VAL :== 0 | 1 
     *   SPACE :== " " 
     *   NEWLINE :== "\r?\n"
     * 
     * If neither FILE nor SIZE_* is given, generate a random board of size 10x10.
     * 
     * Note that FILE and SIZE_* may not be specified simultaneously.
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = 4444; // default port
        Integer sizeX = 10; // default size
        Integer sizeY = 10; // default size
        File file = null;

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--debug")) {
                        debug = true;
                    } else if (flag.equals("--no-debug")) {
                        debug = false;
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > 65535) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else if (flag.equals("--size")) {
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        file = null;
                    } else if (flag.equals("--file")) {
                        sizeX = sizeY = null;
                        file = new File(arguments.remove());
                        if ( ! file.isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file + "\"");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug] [--port PORT] [--size SIZE | --file FILE]");
            return;
        }

        try {
            runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file. Either the file or the size argument must be null, but not both.
     * 
     * @param debug The server should disconnect a client after a BOOM message if and only if this
     *              argument is false.
     * @param sizeX If this argument is not null, start with a random board with width sizeX
     * @param sizeY If this argument is not null, start with a random board with height sizeY
     * @param file If this argument is not null, start with a board loaded from the specified file,
     *             according to the input file format defined in the JavaDoc for main().
     * @param port The network port on which the server should listen.
     */
    public static void runMinesweeperServer(boolean debug, File file, Integer sizeX, Integer sizeY, int port) throws IOException {
        // Initialize MineSweeper board
        BufferedReader in = null;
        // Eclipse cmd-line argument:    --file .\src\minesweeper\server\testBoard.txt
        if(file != null){                        
            try {
                in = new BufferedReader(new FileReader(file));                
                String boardRow, numRows = null, numCols = null;
                int line = 0; 
                List<Integer> bombLocations = new ArrayList<Integer>(); // bombs location storage
                
                while ((boardRow = in.readLine()) != null) {
                    //  get board size
                    if(line == 0){
                        numRows = boardRow.substring(0, 1);
                        numCols = boardRow.substring(2);
                    }
                    // get bombs locations                    
                    String[] boardRowNoSpaces = boardRow.split(" "); // Eliminate white spaces from the row
                    for (int i = 0; i < boardRowNoSpaces.length; i++) {
                        if(boardRowNoSpaces[i].charAt(0) == 'B'){
                            bombLocations.add(i);   // add X location
                            bombLocations.add(line -1); // add Y location
                        }
                    }                    
                    line++;
                }
                // init board
                board = new SimpleBoard(Integer.parseInt(numRows), Integer.parseInt(numCols), bombLocations.size()/2);                
                // insert bombs
                for (int i = 0; i < bombLocations.size(); i = i + 2) { // get bomb locations incrementing by 2, as i = X, i+1 = Y
                    board.changeCellState(bombLocations.get(i), bombLocations.get(i + 1), 'B');                   
                }        
            } catch(IOException e){
                e.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }                
            }
        } else {
            //init board from sizes*
            board = new SimpleBoard(sizeY, sizeX, Integer.MAX_VALUE);
        }
        //System.out.println("Get board state!");
        //System.out.println(board.getBoardState());
        
        
        MinesweeperServer server = new MinesweeperServer(port, debug);
        server.serve();
    }
    
    /**
     * 
     * @return all active users, connected to server.
     */
    public  static int getConnectedPlayers(){
        // synchronize only this part of server
        synchronized(lock){
            return connectedPlayers;
        }        
    }
    
    /**
     * Decrease by 1 current number of active players.
     */
    public static void decreaseNumPlayers(){
        // synchronize only this part of server
        synchronized(lock){
            connectedPlayers--;
        }
    }
    
    /**
     * Return server running mode status. True if sever in debugging mode, otherwise in production mode. 
     * @return
     */
    public static boolean isDebugging(){
        return debug;
    }
}

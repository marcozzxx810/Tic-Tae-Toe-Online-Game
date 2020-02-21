import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

/**
 *  Gamee Class
 *  TicTacToe game class which hold the game logic mainly
 *
 * @author MAK CHAK WING
 * @version Nov 17, 2019
 */
class Game {
    public Player CurrentGamePlayer;

    private Player[] playingboard = new Player[9];

    /**
     * Check is there a winner in the playingboard
     * @return true when there is winner, false when there is no winner
     */
    public boolean CheckWinner(){
        return (playingboard[0] != null && playingboard[0] == playingboard[1] && playingboard[0] == playingboard[2])
                || (playingboard[3] != null && playingboard[3] == playingboard[4] && playingboard[3] == playingboard[5])
                || (playingboard[6] != null && playingboard[6] == playingboard[7] && playingboard[6] == playingboard[8])
                || (playingboard[0] != null && playingboard[0] == playingboard[3] && playingboard[0] == playingboard[6])
                || (playingboard[1] != null && playingboard[1] == playingboard[4] && playingboard[1] == playingboard[7])
                || (playingboard[2] != null && playingboard[2] == playingboard[5] && playingboard[2] == playingboard[8])
                || (playingboard[0] != null && playingboard[0] == playingboard[4] && playingboard[0] == playingboard[8])
                || (playingboard[2] != null && playingboard[2] == playingboard[4] && playingboard[2] == playingboard[6]
        );
    }

    /**
     * Checking is the playingboard fully filled
     * @return true when the board is fully filled , false when the board is not fully filled
     */
    public boolean fullyFilled(){
        return Arrays.stream(playingboard).allMatch(p -> p != null);
    }

    /**
     * checking is that move a valid move by
     Criteria for a valid move:
     - The move is not occupied by any mark.
     - The move is made in the player’s turn.
     - The move is made within the 3 x 3 board.
     * @param player player that make the move
     * @param location 0-9 location in the playingboard
     */
    public synchronized void validMove(Player player, int location){
        if ( player.name_submitted == false) {
            throw new IllegalStateException("Name is not submitted");
        } else if ( player.opponent == null ){
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (player.opponent.name_submitted == false){
            throw new IllegalStateException("Your opponent hasnt input the name");
        }else if ( playingboard[location] != null){
            throw new IllegalStateException("Cell already occupied");
        } else if (player != CurrentGamePlayer){
            throw new IllegalStateException("Not your turn");
        }


        playingboard[location] = CurrentGamePlayer;
        CurrentGamePlayer = CurrentGamePlayer.opponent;
    }

    /**
     *  Player Class
     *  Player in the TicTacToe Game
     *
     * @author MAK CHAK WING
     * @version Nov 17, 2019
     */
    class Player implements Runnable {
        char marks;
        Socket socket;
        Player opponent;
        Scanner in;
        PrintWriter out;
        Boolean name_submitted = false;

        /**
         * The constructor of the player
         * @param socket server socket
         * @param marks either X mark or the O mark
         */
        public Player(Socket socket, char marks) {
            this.marks = marks;
            this.socket = socket;
        }

        /**
         * Setting up the player for input and output
         * @throws IOException when the socket doesnt exist
         */
        private void setup() throws IOException {
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("WELCOME " + marks);
            if(marks == 'X'){
                CurrentGamePlayer = this;
                out.println("MESSAGE Waiting for opponent to connect");
            } else {
                opponent = CurrentGamePlayer;
                opponent.opponent = this;
                opponent.out.println("MESSAGE YOUR MOVE");
            }
        }

        /**
         * Reading Commands ( QUIT, MOVE [Loc] , Name )
         */
        private void readingCommands(){
            while (in.hasNextLine()){
                String command = in.nextLine();
                if (command.startsWith("QUIT")){
                    return;
                } else if (command.startsWith("MOVE")){
                    System.out.println(command);
                    moveCommands(Integer.parseInt(command.substring(5)));
                } else if ( command.startsWith("Name")){
                    this.name_submitted = true;
                    if (this.opponent!=null){
                        if(this.opponent.name_submitted == true){
                            CurrentGamePlayer.out.println("MESSAGE YOUR MOVE");
                        }
                    }
                }

            }
        }

        /**
         * Processing moveCommands
         * Checking valid move , send to opponent the location that moved, check winner and fullyfilled
         * @param location 0-9 in the playingboard
         */
        private void moveCommands(int location) {
            try {
                validMove(this,location);
                out.println("VALID_MOVE");
                opponent.out.println("OPPONENT_MOVED " + location );
                if(CheckWinner()){
                    out.println("VICTORY");
                    opponent.out.println("DEFEAT");
                } else if(fullyFilled()){
                    out.println("TIE");
                    opponent.out.println("TIE");
                }
            } catch (IllegalStateException e) {
                out.println("MESSAGE " + e.getMessage());
            }
        }

        /**
         * Running thread for the player class
         */
        @Override
        public void run() {
            try {
                setup();
                readingCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if ( opponent != null && opponent.out != null) {
                    opponent.out.println("OTHER_PLAYER_LEFT");
                }
                try { socket.close();} catch (IOException e) {}
            }
        }
    }
}
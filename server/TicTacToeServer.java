import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TicTacToeServer {

    public static void main(String[] args) throws Exception {
        try ( ServerSocket listener = new ServerSocket(5001)) {
            System.out.println("Tic Tac Toe Server is Running...");
            ExecutorService pool = Executors.newFixedThreadPool(2);
            while (true) {
                Game game = new Game();
                pool.execute(game.new Player(listener.accept(), 'X'));
                pool.execute(game.new Player(listener.accept(), 'O'));
            }
        }
    }
}
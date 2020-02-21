import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;



public class TicTacToeClient {

    private JFrame frame = new JFrame("Tic Tac Toe");
    public JLabel messageLabel = new JLabel("Enter your player name...");
    private JTextField txt_name = new JTextField();
    public JButton btn_Submit;

    private Square[] board = new Square[9];
    private Square currentSquare;

    private Socket socket;
    private Scanner in;
    private PrintWriter out;

    private Boolean name_Submited = false;
    private String name;

    public TicTacToeClient(String serverAddress) throws Exception {

        socket = new Socket(serverAddress, 5001);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);

        //messageLabel.setBackground(Color.BLACK);
        frame.getContentPane().add(messageLabel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.green);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);

        messageLabel.setForeground(Color.RED);
        //Setup the textfield
        txt_name = new JTextField();
        txt_name.setPreferredSize(new Dimension(200,30));

        //Setup the btn_submit
        btn_Submit = new JButton("Submit");
        btn_Submit.setSize(10,10);

        //setup Name Panel
        JPanel namePanel = new JPanel();
        namePanel.setBackground(Color.black);
        namePanel.setLayout( new FlowLayout(FlowLayout.TRAILING));
        namePanel.add(txt_name);
        namePanel.add(btn_Submit);

        frame.getContentPane().add(namePanel, BorderLayout.SOUTH);
        frame.getContentPane().setBackground(Color.BLACK);

        //JMenuBar Init
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu Control = new JMenu("Control");
        menuBar.add(Control);
        JMenuItem exit = new JMenuItem("Exit");
        Control.add(exit);

        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JMenu help = new JMenu("Help");
        menuBar.add(help);
        JMenuItem Instruction = new JMenuItem("Instruction");
        help.add(Instruction);
        Instruction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame," \n" +
                        "Criteria for a valid move:\n" +
                        "- The move is not occupied by any mark.\n" +
                        "- The move is made in the player’s turn.\n" +
                        "- The move is made within the 3 x 3 board.\n" +
                        "The game would continue and switch among the opposite player until it reaches either one of the following conditions:\n" +
                        "- Player 1 wins.\n" +
                        "- Player 2 wins. \n " +
                        "- Draw.");
            }
        });



        btn_Submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                name_Submited=true;
                name=txt_name.getText();
                System.out.println(name_Submited);
                System.out.println(name);
                frame.setTitle("Tic Tac Toe-Player : " + name );
                messageLabel.setText("WELCOME "+ name);
                txt_name.setEditable(false);
                btn_Submit.setEnabled(false);
                out.println("Name");
            }
        });

    }

    public void setup() throws Exception {
        Thread readerThread = new Thread( new IncomingReader());
        readerThread.start();

    }

    public class IncomingReader implements Runnable   {


        @Override
        public void run() {
            try {
                System.out.println("connecting");
                String response = in.nextLine();
                char mark = response.charAt(8);
                char opponentMark = mark == 'X' ? 'O' : 'X';
                while (in.hasNextLine() ) {
                    System.out.println("connected" );
                    response = in.nextLine();
                    if (response.startsWith("VALID_MOVE")) {
                        messageLabel.setText("Valid move, please wait");
                        currentSquare.setText(mark);
                        currentSquare.repaint();
                    } else if (response.startsWith("OPPONENT_MOVED")) {
                        int loc = Integer.parseInt(response.substring(15));
                        board[loc].setText(opponentMark);
                        board[loc].repaint();
                        messageLabel.setText("Opponent moved, your turn");
                    } else if (response.startsWith("MESSAGE")) {
                        messageLabel.setText(response.substring(8));
                    } else if (response.startsWith("VICTORY")) {
                        JOptionPane.showMessageDialog(frame, "Winner Winner");
                        break;
                    } else if (response.startsWith("DEFEAT")) {
                        JOptionPane.showMessageDialog(frame, "Sorry you lost");
                        break;
                    } else if (response.startsWith("TIE")) {
                        JOptionPane.showMessageDialog(frame, "Tie");
                        break;
                    } else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                        JOptionPane.showMessageDialog(frame, "Other player left");
                        break;
                    }

                    if(name_Submited == false){
                        messageLabel.setText("Enter your player name...");
                    }
                }
                out.println("QUIT");
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                frame.dispose();
            }
        }
    }

    static class Square extends JPanel {
        JLabel label = new JLabel();

        public Square() {
            setBackground(Color.BLACK);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Arial", Font.BOLD, 40));
            add(label);
        }

        public void setText(char text) {
            label.setForeground(text == 'X' ? Color.BLUE : Color.RED);
            label.setText(text + "");
        }
    }


    public static void main(String[] args) throws Exception {

        TicTacToeClient client = new TicTacToeClient("127.0.0.1");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setSize(320, 320);
        client.frame.setVisible(true);
        client.frame.setResizable(false);
        client.setup();

    }
}
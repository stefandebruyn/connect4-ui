import java.awt.Image;

import java.net.URL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;


public class GameUI extends javax.swing.JFrame {
    private final String HIGHSCORE_FILE_NAME = "highscores.dat";
    private Spot[][] board;
    private JButton[] placeButtons;
    private JLabel[] highScoreLabels;
    private SpotState playerTurn;
    private String redName, blueName;
    private Queue<Score> highScoreList = new PriorityQueue<>();
    private int redMoves, blueMoves;
    
    
    /**
     * Boot the game
     */
    public GameUI() {
        initComponents();
        
        redName = JOptionPane.showInputDialog("Enter red player name:");
        blueName = JOptionPane.showInputDialog("Enter blue player name:");
        
        redMoves = 0;
        blueMoves = 0;
        
        board = new Spot[][] {
            { new Spot(btSpot0x0), new Spot(btSpot0x1), new Spot(btSpot0x2), new Spot(btSpot0x3), new Spot(btSpot0x4), new Spot(btSpot0x5), new Spot(btSpot0x6) },
            { new Spot(btSpot1x0), new Spot(btSpot1x1), new Spot(btSpot1x2), new Spot(btSpot1x3), new Spot(btSpot1x4), new Spot(btSpot1x5), new Spot(btSpot1x6) },
            { new Spot(btSpot2x0), new Spot(btSpot2x1), new Spot(btSpot2x2), new Spot(btSpot2x3), new Spot(btSpot2x4), new Spot(btSpot2x5), new Spot(btSpot2x6) },
            { new Spot(btSpot3x0), new Spot(btSpot3x1), new Spot(btSpot3x2), new Spot(btSpot3x3), new Spot(btSpot3x4), new Spot(btSpot3x5), new Spot(btSpot3x6) },
            { new Spot(btSpot4x0), new Spot(btSpot4x1), new Spot(btSpot4x2), new Spot(btSpot4x3), new Spot(btSpot4x4), new Spot(btSpot4x5), new Spot(btSpot4x6) },
            { new Spot(btSpot5x0), new Spot(btSpot5x1), new Spot(btSpot5x2), new Spot(btSpot5x3), new Spot(btSpot5x4), new Spot(btSpot5x5), new Spot(btSpot5x6) }
        };
        placeButtons = new JButton[] {
            btPlace0, btPlace1, btPlace2, btPlace3, btPlace4, btPlace5, btPlace6
        };
        highScoreLabels = new JLabel[] {
            labHighScore1, labHighScore2, labHighScore3, labHighScore4, labHighScore5, labHighScore6, labHighScore7, labHighScore8, labHighScore9, labHighScore10
        };
        playerTurn = (Math.random() < 0.5 ? SpotState.BLUE : SpotState.RED);
        
        labTurnIndicator.setText("");
        loadHighScores();
        updateTurn();
    }
    
    
    
    /**
     * Score in a column
     * 
     * @param column Column index
     */
    private void score(int column) {
        // Update board
        int pos = -1;
        
        for (int i = board.length-1; i >= 0; i--)
            if (board[i][column].getState() == SpotState.EMPTY) {
                board[i][column].setState(playerTurn);
                pos = i;
                break;
            }
        
        if (pos == 0)
            placeButtons[column].setEnabled(false);
        
        if (playerTurn == SpotState.RED)
            redMoves++;
        else
            blueMoves++;
        
        // Check for win
        SpotState winner = checkForWin();
        
        if (winner != SpotState.EMPTY) {
            for (JButton button : placeButtons)
                button.setEnabled(false);
            
            labTurnText.setText(winner == SpotState.BLUE ? blueName + " wins!" : redName + " wins!");
            labTurnMoveCount.setText(playerTurn == SpotState.RED ? redMoves + " moves" : blueMoves + " moves");
            
            saveHighScores();
            
            return;
        }
        
        // Check for stalemate
        boolean vacancy = false;
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++)
                if (board[i][j].getState() == SpotState.EMPTY) {
                    vacancy = true;
                    break;
                }
            
            if (vacancy)
                break;
        }
        
        if (winner == SpotState.EMPTY && !vacancy) {
            labTurnText.setText("Stalemate!");
            labTurnMoveCount.setText("No one wins!");
            
            return;
        }
        
        // Continue
        playerTurn = (playerTurn == SpotState.RED ? SpotState.BLUE : SpotState.RED);
        
        updateTurn();
    }
    
    
    
    /**
     * Update turn UI
     */
    private void updateTurn() {
        // Update turn indicator
        String path = "";
        
        switch (playerTurn) {
            case RED:
                path = "/red.png";
            break;
                
            case BLUE:
                path = "/blue.png";
            break;
                
            case EMPTY:
                path = "/empty.png";
            break;
        }
        
        URL source = getClass().getResource(path);
        ImageIcon icon = new ImageIcon(source);
        ImageIcon resized = new ImageIcon(icon.getImage().getScaledInstance(labTurnIndicator.getWidth(), labTurnIndicator.getHeight(), Image.SCALE_SMOOTH));
        
        labTurnIndicator.setIcon(resized);
        
        // Update turn text
        labTurnText.setText(playerTurn == SpotState.RED ? redName + "'s turn" : blueName + "'s turn");
        labTurnMoveCount.setText(playerTurn == SpotState.RED ? redMoves + " moves" : blueMoves + " moves");
    }
    
    
    
    /**
     * Check for a four-in-a-row
     * 
     * @return Winning color, or SpotState.EMPTY if no one has won yet
     */
    private SpotState checkForWin() {
        for (int i = 0; i < board.length; i++)
            for (int j = 0; j < board[i].length; j++) {
                Spot spot = board[i][j];
                SpotState color = spot.getState();
                
                // Empty spot; continue
                if (color == SpotState.EMPTY)
                    continue;
                
                ArrayList<Spot> spots = new ArrayList<>();
                
                // Check horizontals
                ArrayList<Spot> horizLeftSpots = new ArrayList<>();
                ArrayList<Spot> horizRightSpots = new ArrayList<>();
                
                horizLeftSpots.add(spot);
                horizRightSpots.add(spot);
                
                for (int k = 1; k <= 3; k++) {
                    try {
                        if (board[i][j+k].getState() == color)
                            horizRightSpots.add(board[i][j+k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                    
                    spots.clear();
                    
                    try {
                        if (board[i][j-k].getState() == color)
                            horizLeftSpots.add(board[i][j-k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                }
                
                if (horizLeftSpots.size() == 4) {
                    for (int m = 0; m < horizLeftSpots.size(); m++)
                        horizLeftSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                } else if (horizRightSpots.size() == 4) {
                    for (int m = 0; m < horizRightSpots.size(); m++)
                        horizRightSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                }
                
                // Check verticals
                ArrayList<Spot> vertUpSpots = new ArrayList<>();
                ArrayList<Spot> vertDownSpots = new ArrayList<>();
                
                vertUpSpots.add(spot);
                vertDownSpots.add(spot);
                
                for (int k = 1; k <= 3; k++) {
                    try {
                        if (board[i-k][j].getState() == color)
                            vertUpSpots.add(board[i][j-k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                    
                    try {
                        if (board[i+k][j].getState() == color)
                            vertDownSpots.add(board[i+k][j]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                }
                
                if (vertUpSpots.size() == 4) {
                    for (int m = 0; m < vertUpSpots.size(); m++)
                        vertUpSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                } else if (vertDownSpots.size() == 4) {
                    for (int m = 0; m < vertDownSpots.size(); m++)
                        vertDownSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                }
                
                // Check diagonals
                ArrayList<Spot> diagUpLeftSpots = new ArrayList<>();
                ArrayList<Spot> diagUpRightSpots = new ArrayList<>();
                ArrayList<Spot> diagDownLeftSpots = new ArrayList<>();
                ArrayList<Spot> diagDownRightSpots = new ArrayList<>();
                
                diagUpLeftSpots.add(spot);
                diagUpRightSpots.add(spot);
                diagDownLeftSpots.add(spot);
                diagDownRightSpots.add(spot);
                
                for (int k = 1; k <= 3; k++) {
                    try {
                        if (board[i-k][j-k].getState() == color)
                            diagUpLeftSpots.add(board[i-k][j-k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                    
                    try {
                        if (board[i-k][j+k].getState() == color)
                            diagUpRightSpots.add(board[i-k][j+k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                    
                    try {
                        if (board[i+k][j-k].getState() == color)
                            diagDownLeftSpots.add(board[i+k][j-k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                    
                    try {
                        if (board[i+k][j+k].getState() == color)
                            diagDownRightSpots.add(board[i+k][j+k]);
                    } catch (ArrayIndexOutOfBoundsException e) {}
                }
                
                if (diagUpLeftSpots.size() == 4) {
                    for (int m = 0; m < diagUpLeftSpots.size(); m++)
                        diagUpLeftSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                } else if (diagUpRightSpots.size() == 4) {
                    for (int m = 0; m < diagUpRightSpots.size(); m++)
                        diagUpRightSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                } else if (diagDownLeftSpots.size() == 4) {
                    for (int m = 0; m < diagDownLeftSpots.size(); m++)
                        diagDownLeftSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                } else if (diagDownRightSpots.size() == 4) {
                    for (int m = 0; m < diagDownRightSpots.size(); m++)
                        diagDownRightSpots.get(m).setState(color == SpotState.RED ? SpotState.RED_WIN : SpotState.BLUE_WIN);
                    return color;
                }
            }
        
        return SpotState.EMPTY;
    }
    
    
    
    /**
     * Load high scores from disk
     */
    private void loadHighScores() {
        Scanner reader;
        File file = new File(HIGHSCORE_FILE_NAME);
        
        try {
            file.createNewFile();
        } catch (IOException e) {}
        
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            return;
        }
        
        while (reader.hasNextLine()) {
            String[] data = reader.nextLine().split("!");
            highScoreList.add(new Score(data[0], Integer.parseInt(data[1])));
        }
        
        reader.close();
        updateHighScoreUI();
    }
    
    
    
    /**
     * Save high scores to disk
     */
    private void saveHighScores() {
        String name = (playerTurn == SpotState.RED ? redName : blueName);
        int moves = (playerTurn == SpotState.RED ? redMoves : blueMoves);
        
        highScoreList.add(new Score(name, moves));
        
        while (highScoreList.size() > highScoreLabels.length)
            highScoreList.poll();
        
        try {
            Iterator<Score> iter = highScoreList.iterator();
            PrintWriter writer = new PrintWriter(new FileWriter(HIGHSCORE_FILE_NAME));
            
            while (iter.hasNext()) {
                Score score = iter.next();

                writer.println(score.getPlayerName() + "!" + score.getScore());
            }
            
            writer.close();
            
        } catch (IOException e) {}
        
        updateHighScoreUI();
    }
    
    
    
    /**
     * Update high score listings
     */
    private void updateHighScoreUI() {
        Queue<Score> clone = new PriorityQueue<>(highScoreList);
        
        for (int i = 0; i < highScoreLabels.length; i++) {
            String str = "Empty";
            
            if (!clone.isEmpty())
                str = clone.poll().toString();
            
            highScoreLabels[i].setText(str);
        }
    }

    
    
    /**
     * Build the UI
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panUI = new javax.swing.JPanel();
        labTurnIndicator = new javax.swing.JLabel();
        labTurnText = new javax.swing.JLabel();
        btExit = new javax.swing.JButton();
        btRestart = new javax.swing.JButton();
        panHighScores = new javax.swing.JPanel();
        labHighScore1 = new javax.swing.JLabel();
        labHighScore2 = new javax.swing.JLabel();
        labHighScore3 = new javax.swing.JLabel();
        labHighScore4 = new javax.swing.JLabel();
        labHighScore5 = new javax.swing.JLabel();
        labHighScore6 = new javax.swing.JLabel();
        labHighScore7 = new javax.swing.JLabel();
        labHighScore8 = new javax.swing.JLabel();
        labHighScore9 = new javax.swing.JLabel();
        labHighScore10 = new javax.swing.JLabel();
        labTurnMoveCount = new javax.swing.JLabel();
        panBoard = new javax.swing.JPanel();
        btSpot0x0 = new javax.swing.JLabel();
        btSpot0x1 = new javax.swing.JLabel();
        btSpot0x2 = new javax.swing.JLabel();
        btSpot0x3 = new javax.swing.JLabel();
        btSpot0x4 = new javax.swing.JLabel();
        btSpot0x5 = new javax.swing.JLabel();
        btSpot0x6 = new javax.swing.JLabel();
        btSpot1x0 = new javax.swing.JLabel();
        btSpot1x1 = new javax.swing.JLabel();
        btSpot1x2 = new javax.swing.JLabel();
        btSpot1x3 = new javax.swing.JLabel();
        btSpot1x4 = new javax.swing.JLabel();
        btSpot1x5 = new javax.swing.JLabel();
        btSpot1x6 = new javax.swing.JLabel();
        btSpot2x0 = new javax.swing.JLabel();
        btSpot2x1 = new javax.swing.JLabel();
        btSpot2x2 = new javax.swing.JLabel();
        btSpot2x3 = new javax.swing.JLabel();
        btSpot2x4 = new javax.swing.JLabel();
        btSpot2x5 = new javax.swing.JLabel();
        btSpot2x6 = new javax.swing.JLabel();
        btSpot3x0 = new javax.swing.JLabel();
        btSpot3x1 = new javax.swing.JLabel();
        btSpot3x2 = new javax.swing.JLabel();
        btSpot3x3 = new javax.swing.JLabel();
        btSpot3x4 = new javax.swing.JLabel();
        btSpot3x5 = new javax.swing.JLabel();
        btSpot3x6 = new javax.swing.JLabel();
        btSpot4x0 = new javax.swing.JLabel();
        btSpot4x1 = new javax.swing.JLabel();
        btSpot4x2 = new javax.swing.JLabel();
        btSpot4x3 = new javax.swing.JLabel();
        btSpot4x4 = new javax.swing.JLabel();
        btSpot4x5 = new javax.swing.JLabel();
        btSpot4x6 = new javax.swing.JLabel();
        btSpot5x0 = new javax.swing.JLabel();
        btSpot5x1 = new javax.swing.JLabel();
        btSpot5x2 = new javax.swing.JLabel();
        btSpot5x3 = new javax.swing.JLabel();
        btSpot5x4 = new javax.swing.JLabel();
        btSpot5x5 = new javax.swing.JLabel();
        btSpot5x6 = new javax.swing.JLabel();
        btPlace0 = new javax.swing.JButton();
        btPlace1 = new javax.swing.JButton();
        btPlace2 = new javax.swing.JButton();
        btPlace3 = new javax.swing.JButton();
        btPlace4 = new javax.swing.JButton();
        btPlace5 = new javax.swing.JButton();
        btPlace6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        panUI.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labTurnText.setText("jLabel1");

        btExit.setText("Exit");
        btExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btExitActionPerformed(evt);
            }
        });

        btRestart.setText("Restart");
        btRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRestartActionPerformed(evt);
            }
        });

        panHighScores.setBorder(javax.swing.BorderFactory.createTitledBorder("Fastest Wins"));

        labHighScore1.setText("jLabel1");

        labHighScore2.setText("jLabel1");

        labHighScore3.setText("jLabel1");

        labHighScore4.setText("jLabel1");

        labHighScore5.setText("jLabel1");

        labHighScore6.setText("jLabel1");

        labHighScore7.setText("jLabel1");

        labHighScore8.setText("jLabel1");

        labHighScore9.setText("jLabel1");

        labHighScore10.setText("jLabel1");

        javax.swing.GroupLayout panHighScoresLayout = new javax.swing.GroupLayout(panHighScores);
        panHighScores.setLayout(panHighScoresLayout);
        panHighScoresLayout.setHorizontalGroup(
            panHighScoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panHighScoresLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panHighScoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labHighScore1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labHighScore10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panHighScoresLayout.setVerticalGroup(
            panHighScoresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panHighScoresLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labHighScore1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labHighScore9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addComponent(labHighScore10)
                .addContainerGap())
        );

        labTurnMoveCount.setText("jLabel1");

        javax.swing.GroupLayout panUILayout = new javax.swing.GroupLayout(panUI);
        panUI.setLayout(panUILayout);
        panUILayout.setHorizontalGroup(
            panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUILayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panUILayout.createSequentialGroup()
                        .addGroup(panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panHighScores, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panUILayout.createSequentialGroup()
                                .addComponent(btRestart)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btExit)
                                .addGap(0, 26, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(panUILayout.createSequentialGroup()
                        .addComponent(labTurnIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labTurnText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(labTurnMoveCount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        panUILayout.setVerticalGroup(
            panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panUILayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labTurnIndicator, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panUILayout.createSequentialGroup()
                        .addComponent(labTurnText, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labTurnMoveCount)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panHighScores, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btRestart)
                    .addComponent(btExit))
                .addContainerGap())
        );

        panBoard.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btSpot0x0.setText("spot");

        btSpot0x1.setText("spot");

        btSpot0x2.setText("spot");

        btSpot0x3.setText("spot");

        btSpot0x4.setText("spot");

        btSpot0x5.setText("spot");

        btSpot0x6.setText("spot");

        btSpot1x0.setText("spot");

        btSpot1x1.setText("spot");

        btSpot1x2.setText("spot");

        btSpot1x3.setText("spot");

        btSpot1x4.setText("spot");

        btSpot1x5.setText("spot");

        btSpot1x6.setText("spot");

        btSpot2x0.setText("spot");

        btSpot2x1.setText("spot");

        btSpot2x2.setText("spot");

        btSpot2x3.setText("spot");

        btSpot2x4.setText("spot");

        btSpot2x5.setText("spot");

        btSpot2x6.setText("spot");

        btSpot3x0.setText("spot");

        btSpot3x1.setText("spot");

        btSpot3x2.setText("spot");

        btSpot3x3.setText("spot");

        btSpot3x4.setText("spot");

        btSpot3x5.setText("spot");

        btSpot3x6.setText("spot");

        btSpot4x0.setText("spot");

        btSpot4x1.setText("spot");

        btSpot4x2.setText("spot");

        btSpot4x3.setText("spot");

        btSpot4x4.setText("spot");

        btSpot4x5.setText("spot");

        btSpot4x6.setText("spot");

        btSpot5x0.setText("spot");

        btSpot5x1.setText("spot");

        btSpot5x2.setText("spot");

        btSpot5x3.setText("spot");

        btSpot5x4.setText("spot");

        btSpot5x5.setText("spot");

        btSpot5x6.setText("spot");

        btPlace0.setText("Place");
        btPlace0.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace0ActionPerformed(evt);
            }
        });

        btPlace1.setText("Place");
        btPlace1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace1ActionPerformed(evt);
            }
        });

        btPlace2.setText("Place");
        btPlace2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace2ActionPerformed(evt);
            }
        });

        btPlace3.setText("Place");
        btPlace3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace3ActionPerformed(evt);
            }
        });

        btPlace4.setText("Place");
        btPlace4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace4ActionPerformed(evt);
            }
        });

        btPlace5.setText("Place");
        btPlace5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace5ActionPerformed(evt);
            }
        });

        btPlace6.setText("Place");
        btPlace6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPlace6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panBoardLayout = new javax.swing.GroupLayout(panBoard);
        panBoard.setLayout(panBoardLayout);
        panBoardLayout.setHorizontalGroup(
            panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBoardLayout.createSequentialGroup()
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panBoardLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btPlace0)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btPlace1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btPlace2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btPlace3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btPlace4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btPlace5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btPlace6))
                    .addGroup(panBoardLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x0)
                            .addComponent(btSpot4x0)
                            .addComponent(btSpot3x0)
                            .addComponent(btSpot2x0)
                            .addComponent(btSpot1x0)
                            .addComponent(btSpot0x0))
                        .addGap(42, 42, 42)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x1)
                            .addComponent(btSpot4x1)
                            .addComponent(btSpot3x1)
                            .addComponent(btSpot2x1)
                            .addComponent(btSpot1x1)
                            .addComponent(btSpot0x1))
                        .addGap(42, 42, 42)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x2)
                            .addComponent(btSpot4x2)
                            .addComponent(btSpot3x2)
                            .addComponent(btSpot2x2)
                            .addComponent(btSpot1x2)
                            .addComponent(btSpot0x2))
                        .addGap(42, 42, 42)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x3)
                            .addComponent(btSpot4x3)
                            .addComponent(btSpot3x3)
                            .addComponent(btSpot2x3)
                            .addComponent(btSpot1x3)
                            .addComponent(btSpot0x3))
                        .addGap(42, 42, 42)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x4)
                            .addComponent(btSpot4x4)
                            .addComponent(btSpot3x4)
                            .addComponent(btSpot2x4)
                            .addComponent(btSpot1x4)
                            .addComponent(btSpot0x4))
                        .addGap(42, 42, 42)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x5)
                            .addComponent(btSpot4x5)
                            .addComponent(btSpot3x5)
                            .addComponent(btSpot2x5)
                            .addComponent(btSpot1x5)
                            .addComponent(btSpot0x5))
                        .addGap(42, 42, 42)
                        .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btSpot5x6)
                            .addComponent(btSpot4x6)
                            .addComponent(btSpot3x6)
                            .addComponent(btSpot2x6)
                            .addComponent(btSpot1x6)
                            .addComponent(btSpot0x6))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panBoardLayout.setVerticalGroup(
            panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panBoardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btPlace0)
                    .addComponent(btPlace1)
                    .addComponent(btPlace2)
                    .addComponent(btPlace3)
                    .addComponent(btPlace4)
                    .addComponent(btPlace5)
                    .addComponent(btPlace6))
                .addGap(25, 25, 25)
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSpot0x0)
                    .addComponent(btSpot0x1)
                    .addComponent(btSpot0x2)
                    .addComponent(btSpot0x3)
                    .addComponent(btSpot0x4)
                    .addComponent(btSpot0x5)
                    .addComponent(btSpot0x6))
                .addGap(32, 32, 32)
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSpot1x0)
                    .addComponent(btSpot1x1)
                    .addComponent(btSpot1x2)
                    .addComponent(btSpot1x3)
                    .addComponent(btSpot1x4)
                    .addComponent(btSpot1x5)
                    .addComponent(btSpot1x6))
                .addGap(31, 31, 31)
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSpot2x0)
                    .addComponent(btSpot2x1)
                    .addComponent(btSpot2x2)
                    .addComponent(btSpot2x3)
                    .addComponent(btSpot2x4)
                    .addComponent(btSpot2x5)
                    .addComponent(btSpot2x6))
                .addGap(30, 30, 30)
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSpot3x0)
                    .addComponent(btSpot3x1)
                    .addComponent(btSpot3x2)
                    .addComponent(btSpot3x3)
                    .addComponent(btSpot3x4)
                    .addComponent(btSpot3x5)
                    .addComponent(btSpot3x6))
                .addGap(32, 32, 32)
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSpot4x0)
                    .addComponent(btSpot4x1)
                    .addComponent(btSpot4x2)
                    .addComponent(btSpot4x3)
                    .addComponent(btSpot4x4)
                    .addComponent(btSpot4x5)
                    .addComponent(btSpot4x6))
                .addGap(32, 32, 32)
                .addGroup(panBoardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSpot5x0)
                    .addComponent(btSpot5x1)
                    .addComponent(btSpot5x2)
                    .addComponent(btSpot5x3)
                    .addComponent(btSpot5x4)
                    .addComponent(btSpot5x5)
                    .addComponent(btSpot5x6))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panUI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panBoard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panBoard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(panUI, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btPlace0ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace0ActionPerformed
        score(0);
    }//GEN-LAST:event_btPlace0ActionPerformed

    private void btPlace1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace1ActionPerformed
        score(1);
    }//GEN-LAST:event_btPlace1ActionPerformed

    private void btPlace2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace2ActionPerformed
        score(2);
    }//GEN-LAST:event_btPlace2ActionPerformed

    private void btPlace3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace3ActionPerformed
        score(3);
    }//GEN-LAST:event_btPlace3ActionPerformed

    private void btPlace4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace4ActionPerformed
        score(4);
    }//GEN-LAST:event_btPlace4ActionPerformed

    private void btPlace5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace5ActionPerformed
        score(5);
    }//GEN-LAST:event_btPlace5ActionPerformed

    private void btPlace6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPlace6ActionPerformed
        score(6);
    }//GEN-LAST:event_btPlace6ActionPerformed

    private void btExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btExitActionPerformed

    private void btRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRestartActionPerformed
        for (Spot[] row : board)
            for (Spot spot : row)
                spot.setState(SpotState.EMPTY);
        
        for (JButton button : placeButtons)
            button.setEnabled(true);
        
        redMoves = 0;
        blueMoves = 0;
        
        updateTurn();
    }//GEN-LAST:event_btRestartActionPerformed

    
    
    /**
     * Boot the game
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GameUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GameUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GameUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GameUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GameUI().setVisible(true);
            }
        });
    }

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btExit;
    private javax.swing.JButton btPlace0;
    private javax.swing.JButton btPlace1;
    private javax.swing.JButton btPlace2;
    private javax.swing.JButton btPlace3;
    private javax.swing.JButton btPlace4;
    private javax.swing.JButton btPlace5;
    private javax.swing.JButton btPlace6;
    private javax.swing.JButton btRestart;
    private javax.swing.JLabel btSpot0x0;
    private javax.swing.JLabel btSpot0x1;
    private javax.swing.JLabel btSpot0x2;
    private javax.swing.JLabel btSpot0x3;
    private javax.swing.JLabel btSpot0x4;
    private javax.swing.JLabel btSpot0x5;
    private javax.swing.JLabel btSpot0x6;
    private javax.swing.JLabel btSpot1x0;
    private javax.swing.JLabel btSpot1x1;
    private javax.swing.JLabel btSpot1x2;
    private javax.swing.JLabel btSpot1x3;
    private javax.swing.JLabel btSpot1x4;
    private javax.swing.JLabel btSpot1x5;
    private javax.swing.JLabel btSpot1x6;
    private javax.swing.JLabel btSpot2x0;
    private javax.swing.JLabel btSpot2x1;
    private javax.swing.JLabel btSpot2x2;
    private javax.swing.JLabel btSpot2x3;
    private javax.swing.JLabel btSpot2x4;
    private javax.swing.JLabel btSpot2x5;
    private javax.swing.JLabel btSpot2x6;
    private javax.swing.JLabel btSpot3x0;
    private javax.swing.JLabel btSpot3x1;
    private javax.swing.JLabel btSpot3x2;
    private javax.swing.JLabel btSpot3x3;
    private javax.swing.JLabel btSpot3x4;
    private javax.swing.JLabel btSpot3x5;
    private javax.swing.JLabel btSpot3x6;
    private javax.swing.JLabel btSpot4x0;
    private javax.swing.JLabel btSpot4x1;
    private javax.swing.JLabel btSpot4x2;
    private javax.swing.JLabel btSpot4x3;
    private javax.swing.JLabel btSpot4x4;
    private javax.swing.JLabel btSpot4x5;
    private javax.swing.JLabel btSpot4x6;
    private javax.swing.JLabel btSpot5x0;
    private javax.swing.JLabel btSpot5x1;
    private javax.swing.JLabel btSpot5x2;
    private javax.swing.JLabel btSpot5x3;
    private javax.swing.JLabel btSpot5x4;
    private javax.swing.JLabel btSpot5x5;
    private javax.swing.JLabel btSpot5x6;
    private javax.swing.JLabel labHighScore1;
    private javax.swing.JLabel labHighScore10;
    private javax.swing.JLabel labHighScore2;
    private javax.swing.JLabel labHighScore3;
    private javax.swing.JLabel labHighScore4;
    private javax.swing.JLabel labHighScore5;
    private javax.swing.JLabel labHighScore6;
    private javax.swing.JLabel labHighScore7;
    private javax.swing.JLabel labHighScore8;
    private javax.swing.JLabel labHighScore9;
    private javax.swing.JLabel labTurnIndicator;
    private javax.swing.JLabel labTurnMoveCount;
    private javax.swing.JLabel labTurnText;
    private javax.swing.JPanel panBoard;
    private javax.swing.JPanel panHighScores;
    private javax.swing.JPanel panUI;
    // End of variables declaration//GEN-END:variables
}

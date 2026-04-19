package src.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

public class GameStart extends JFrame {
    private final String ASSETS_PATH = "/src/assets/";
    private boolean isShowingNameEntryScreen = false;
    public GameStart(){
        setTitle("Ticket To Ride: London");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                if(isShowingNameEntryScreen){
                    showScreen();
                }else{
                    System.exit(0);
                }
            }
        });

        showScreen();
        setVisible(true);
    }
    private void showScreen(){
        isShowingNameEntryScreen = false;
        setSize(600,600);
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel background = new JPanel(){
          @Override
          protected void paintComponent(Graphics g){
              super.paintComponent(g);
              ImageIcon icon = new ImageIcon(getClass().getResource( ASSETS_PATH + "gameStart.png"));
              g.drawImage(icon.getImage(), 0,0, getWidth(), getHeight(), this);
          }
        };
        background.setLayout(new GridBagLayout());

        JButton playButton = new JButton("");
        playButton.setPreferredSize(new Dimension(150, 150));
        playButton.setOpaque(false);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setFocusPainted(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        playButton.addActionListener(e -> showNameEntry());

        background.add(playButton);
        add(background, BorderLayout.CENTER);

        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }

    private void showNameEntry(){
        isShowingNameEntryScreen = true;
        getContentPane().removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField p1Field = new JTextField("Player 1", 15);
        JTextField p2Field = new JTextField("Player 2", 15);
        JButton startButton = new JButton("START GAME");
        JButton cancelButton = new JButton("CANCEL");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Player 1 Name:"), gbc);

        gbc.gridx = 1;
        add(p1Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Player 2 Name:"), gbc);

        gbc.gridx = 1;
        add(p2Field, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        add(startButton, gbc);

        gbc.gridy = 3;
        add(cancelButton, gbc);

        cancelButton.addActionListener(e -> showScreen());

        startButton.addActionListener(e -> {
            List<String> playerNames = new ArrayList<String>();
            playerNames.add(p1Field.getText());
            playerNames.add(p2Field.getText());

            this.dispose();
            new MainFrame(playerNames);
        });

        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
    }
}

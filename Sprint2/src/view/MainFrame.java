package src.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    Color [] playerColor = {new Color(220, 53, 64), new Color(0, 123, 255), new Color(40, 167, 69), new Color(255, 193, 7)};

    public MainFrame() {
        setTitle("London Map - Game On");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(getMaximumSize());
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        JPanel boardPanel = new JPanel();
        boardPanel.setBorder(BorderFactory.createTitledBorder("Board"));
        boardPanel.setBackground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.9;
        gbc.weighty = 0.9;
        add(boardPanel, gbc);

        JPanel playerPanel = new JPanel();
        playerPanel.setBorder(BorderFactory.createTitledBorder("Current Players' Hand"));
        playerPanel.setBackground(Color.WHITE);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.4;
        add(playerPanel, gbc);

        JPanel playerScores = new JPanel(new GridLayout(1,3,10,10));
        playerScores.setBorder(BorderFactory.createTitledBorder("Scores"));
        for (int i = 0; i < 2; i++){
            JLabel playerLabel = new JLabel("Player " + (i+1) + ": 10 ", SwingConstants.CENTER);
            playerLabel.setForeground(playerColor[i]);
            playerScores.add(playerLabel);
        }
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 0.05;
        add(playerScores, gbc);

        JPanel actionPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        actionPanel.add(new JButton("Claim Route"));
        actionPanel.add(new JButton("Draw Cards"));
        actionPanel.add(new JButton("Draw Tickets"));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weighty = 0.15;
        add(actionPanel, gbc);

        JPanel decksPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        decksPanel.setBorder(BorderFactory.createTitledBorder("Draw Decks"));
        JButton trainDeck = new JButton("<html><center>Train Car<br>Deck</center></html>");
        JButton ticketDeck = new JButton("<html><center>Destination<br>Deck</center></html>");
        decksPanel.add(trainDeck);
        decksPanel.add(ticketDeck);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.weighty = 0.2;
        add(decksPanel, gbc);

        JPanel faceUpPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        faceUpPanel.setBorder(BorderFactory.createTitledBorder("Face-Up Cards"));
        for (int i = 1; i <= 5; i++) {
            faceUpPanel.add(new JButton("Card " + i));
        }
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(faceUpPanel, gbc);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}

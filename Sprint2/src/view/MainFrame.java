package src.view;

import src.model.GameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {
    private final BoardPanel boardPanel;
    private final PlayerHandPanel playerHandPanel;
    private final PlayerScorePanel playerScorePanel;
    private final ActionsPanel actionsPanel;
    private final DeckPanel deckPanel;
    private final FaceUpPanel faceUpPanel;

    private final GameEngine gameEngine;

    public MainFrame(List<String> playerNames) {
        setTitle("London Map - Game On");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        gameEngine = new GameEngine(playerNames);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                int response = JOptionPane.showConfirmDialog(null, "Are you sure to exit the game?", "Confirm Game Exit", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }else{
                    e.getWindow().dispose();
                }
            }
        });

//        setSize(getMaximumSize());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridBagLayout());

        boardPanel = new BoardPanel(gameEngine);
        playerHandPanel = new PlayerHandPanel(gameEngine);
        playerScorePanel = new PlayerScorePanel(gameEngine);
        actionsPanel = new ActionsPanel();
        deckPanel = new DeckPanel(gameEngine);
        faceUpPanel = new FaceUpPanel(gameEngine);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.9;
        gbc.weighty = 0.9;
        add(boardPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.4;
        add(playerHandPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 0.05;
        add(playerScorePanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weighty = 0.15;
        add(actionsPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.weighty = 0.2;
        add(deckPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(faceUpPanel, gbc);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public BoardPanel getBoardPanel(){
        return boardPanel;
    }

    public JButton getDrawCardsButton(){
        return actionsPanel.getDrawCardsButton();
    }

    public JButton getDrawTicketsButton() {
        return actionsPanel.getDrawTicketsButton();
    }

    public JButton getFaceUpButton(int index) {
        return faceUpPanel.getCardButton(index);
    }

    public GameEngine getEngine() {
        return gameEngine;
    }
}

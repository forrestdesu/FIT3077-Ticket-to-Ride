package src.view;

import src.interfaces.GameObserver;
import src.model.GameEngine;
import src.model.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerScorePanel extends JPanel implements GameObserver {
    private static final Color[] PLAYER_COLORS = {new Color(220, 53, 64), new Color(0, 123, 255), new Color(40, 167, 69), new Color(255, 193, 7)};
    private final GameEngine gameEngine;
    private final List<JLabel> scoreLabels;

    public PlayerScorePanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);

        List<Player> players = gameEngine.getPlayers();
        setLayout(new GridLayout(1, 2, 5, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Scores"));

        scoreLabels = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            JLabel label = new JLabel("", JLabel.CENTER);
            label.setForeground(PLAYER_COLORS[i % PLAYER_COLORS.length]);
            label.setFont(new Font("Arial", Font.BOLD, 16));
            scoreLabels.add(label);
            add(label);
        }
    }

    private void refresh() {
        List<Player> players = gameEngine.getPlayers();
        Player current = gameEngine.getCurrentPlayer();

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            String text = p.getPlayerName() + ": " + p.getPlayerScore() + " pts  (" + p.getTrainPiecesRemaining()
                    + " trains)";
            JLabel lbl = scoreLabels.get(i);
            lbl.setText(text);

            // Underline + larger font for the active player
            if (p == current) {
                lbl.setFont(new Font("Arial", Font.BOLD, 16));
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0,
                        PLAYER_COLORS[i % PLAYER_COLORS.length]));
            } else {
                lbl.setFont(new Font("Arial", Font.PLAIN, 14));
                lbl.setBorder(null);
            }
        }
    }

    @Override
    public void onGameStateChange() {
        refresh();
    }
}

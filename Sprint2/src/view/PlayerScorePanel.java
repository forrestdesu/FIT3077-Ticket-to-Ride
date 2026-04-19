package src.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlayerScorePanel extends JPanel {
    Color[] playerColors = {new Color(220, 53, 64), new Color(0, 123, 255), new Color(40, 167, 69), new Color(255, 193, 7)};
    public PlayerScorePanel(List<String> playerNames) {
        setLayout(new GridLayout(1, 2, 5, 5));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder("Scores"));

        for (int i = 0; i < playerNames.size(); i++) {
            JLabel scoreLabel = new JLabel(playerNames.get(i) + ": 0", SwingConstants.CENTER);
            scoreLabel.setForeground(playerColors[i]);
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
            add(scoreLabel);
        }
    }
}

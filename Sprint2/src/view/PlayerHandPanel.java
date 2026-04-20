package src.view;

import src.interfaces.GameObserver;
import src.model.GameEngine;
import src.model.TrainCarCard;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class PlayerHandPanel extends JPanel implements GameObserver {
    private final GameEngine gameEngine;
    public PlayerHandPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);

        setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4));
        setBorder(BorderFactory.createTitledBorder("Current Player's Hand"));
        setBackground(Color.WHITE);

        refresh();
    }

    private void refresh() {
        removeAll();

        JLabel whose = new JLabel(gameEngine.getCurrentPlayer().getPlayerName() + ":");
        whose.setFont(new Font("Arial", Font.BOLD, 12));
        whose.setForeground(gameEngine.getCurrentPlayer().getPlayerColor());
        add(whose);

        Map<TrainCarCard.CardColor, Integer> counts = gameEngine.getCurrentPlayer().getPlayerhand().getCardColorCounts();

        if (counts.isEmpty()) {
            JLabel empty = new JLabel("(no cards)");
            empty.setFont(new Font("Arial", Font.ITALIC, 11));
            add(empty);
        } else {
            for (TrainCarCard.CardColor color : TrainCarCard.CardColor.values()) {
                Integer count = counts.get(color);
                if (count != null && count > 0) {
                    JLabel lbl = new JLabel(color.toString() + "×" + count);
                    lbl.setFont(new Font("Arial", Font.BOLD, 11));
                    lbl.setOpaque(true);
                    lbl.setBackground(cardColorToAwt(color));
                    lbl.setForeground(textColorFor(color));
                    lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                    add(lbl);
                }
            }
        }

        revalidate();
        repaint();
    }

    private Color cardColorToAwt(TrainCarCard.CardColor color) {
        switch (color) {
            case RED:    return new Color(210, 60,  60);
            case BLUE:   return new Color(60,  110, 210);
            case GREEN:  return new Color(50,  160, 60);
            case YELLOW: return new Color(220, 190, 20);
            case BLACK:  return new Color(40,  40,  40);
            case WHITE:  return new Color(220, 220, 220);
            case ORANGE: return new Color(220, 120, 30);
            case PINK:   return new Color(210, 100, 160);
            case LOCOMOTIVE:   return new Color(140, 80,  200);
            default:     return Color.GRAY;
        }
    }

    private Color textColorFor(TrainCarCard.CardColor color) {
        return (color == TrainCarCard.CardColor.WHITE || color == TrainCarCard.CardColor.YELLOW) ? Color.BLACK : Color.WHITE;
    }

    @Override
    public void onGameStateChange() {
        refresh();
    }
}

package src.view;

import src.interfaces.GameObserver;
import src.model.GameEngine;
import src.model.TrainCarCard;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FaceUpPanel extends JPanel implements GameObserver {
    private final GameEngine gameEngine;
    private final List<JButton> faceUpCardsButton;

    public FaceUpPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);

        setLayout(new GridLayout(1, 5, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Face-Up Cards"));
        faceUpCardsButton = new ArrayList<>();

        for (int i = 1; i <= GameEngine.FACE_UP_CARD_COUNT; i++) {
            JButton btn = new JButton("Card " + i);
            btn.setPreferredSize(new Dimension(80, 50));
            btn.setFont(new Font("Arial", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setBorderPainted(true);
            faceUpCardsButton.add(btn);
            add(btn);
        }
        refresh();
    }

    public JButton getCardButton(int index) {
        return faceUpCardsButton.get(index);
    }

    private void refresh() {
        List<TrainCarCard> faceUp = gameEngine.getFaceUpCards();
        for (int i = 0; i < faceUpCardsButton.size(); i++) {
            JButton btn = faceUpCardsButton.get(i);
            if (i < faceUp.size()) {
                TrainCarCard card = faceUp.get(i);
                String label = card.getCardColor() == TrainCarCard.CardColor.LOCOMOTIVE ? "LOCO" : card.getCardColor().toString();
                btn.setText("<html><center>" + label + "</center></html>");
                Color bg = cardColorToAwt(card.getCardColor());
                btn.setBackground(bg);
                btn.setForeground(textColorFor(card.getCardColor()));
                btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bg.darker().darker(), 3), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
                btn.setEnabled(true);
            } else {
                btn.setText("—");
                btn.setBackground(new Color(200, 200, 200));
                btn.setForeground(Color.DARK_GRAY);
                btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
                btn.setEnabled(false);
            }
        }
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

package src.view;

import src.interfaces.GameObserver;
import src.model.DestinationTicket;
import src.model.GameEngine;
import src.model.Player;
import src.model.TrainCarCard;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlayerHandPanel extends JPanel implements GameObserver {
    private final GameEngine gameEngine;
    private static final int CARD_WIDTH = 70;
    private static final int CARD_HEIGHT = 40;

    public PlayerHandPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);

        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createTitledBorder("Current Player's Hand"));
        setBackground(Color.WHITE);

        refresh();
    }

    private void refresh() {
        removeAll();

        Player player = gameEngine.getCurrentPlayer();

        JLabel header = new JLabel("  " + player.getPlayerName() + "'s Hand", JLabel.LEFT);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setForeground(player.getPlayerColor());
        header.setOpaque(true);
        header.setBackground(new Color(230, 225, 210));
        header.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(header, BorderLayout.NORTH);

        JPanel cardGrid = new JPanel(new GridLayout(3, 3, 6, 6));
        cardGrid.setBackground(new Color(248, 246, 238));
        cardGrid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (TrainCarCard.CardColor color : TrainCarCard.CardColor.values()) {
            int count = player.getPlayerhand().cardColorCount(color);
            cardGrid.add(makeCardTile(color, count));
        }

        add(cardGrid, BorderLayout.CENTER);

        List<DestinationTicket> tickets = player.getPlayerTickets();
        JPanel ticketPanel = buildTicketPanel(tickets);
        add(ticketPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel makeCardTile(TrainCarCard.CardColor color, int count) {
        JPanel tile = new JPanel(new BorderLayout());
        tile.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        tile.setOpaque(true);

        boolean hasCards = count > 0;

        Color bg = hasCards ? cardColorToAwt(color) : washOut(cardColorToAwt(color));
        tile.setBackground(bg);

        if (hasCards) {
            tile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bg.darker(), 3), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        } else {
            tile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        }

        String colorName = color == TrainCarCard.CardColor.LOCOMOTIVE ? "LOCOMOTIVE" : color.toString();
        JLabel nameLabel = new JLabel(colorName, JLabel.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 11));
        nameLabel.setForeground(hasCards ? textColorFor(color) : new Color(150, 150, 150));

        JLabel countLabel = new JLabel("×" + count, JLabel.CENTER);
        countLabel.setFont(new Font("Arial", Font.BOLD, hasCards ? 18 : 14));
        countLabel.setForeground(hasCards ? textColorFor(color) : new Color(150, 150, 150));

        tile.add(nameLabel, BorderLayout.NORTH);
        tile.add(countLabel, BorderLayout.CENTER);

        return tile;
    }

    private JPanel buildTicketPanel(List<DestinationTicket> tickets) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(new Color(248, 246, 238));
        outer.setBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 170, 140)), "Destination Tickets"));

        JPanel list = new JPanel(new GridLayout(0, 1, 2, 2));
        list.setBackground(new Color(248, 246, 238));

        if (tickets.isEmpty()) {
            JLabel none = new JLabel("  (none yet)", JLabel.LEFT);
            none.setFont(new Font("Arial", Font.ITALIC, 11));
            none.setForeground(Color.GRAY);
            list.add(none);
        } else {
            for (DestinationTicket ticket : tickets) {
                JLabel lbl = new JLabel("  " + ticket.getCity1() + " ↔ " + ticket.getCity2() + "  [" + ticket.getPoints() + " pts]", JLabel.LEFT);
                lbl.setFont(new Font("Arial", Font.PLAIN, 14));
                lbl.setOpaque(true);
                lbl.setBackground(new Color(255, 252, 230));
                lbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 185, 120), 1), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                list.add(lbl);
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(248, 246, 238));
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
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

    private Color washOut(Color base) {
        int r = base.getRed()   + (255 - base.getRed())   * 6 / 8;
        int g = base.getGreen() + (255 - base.getGreen()) * 6 / 8;
        int b = base.getBlue()  + (255 - base.getBlue())  * 6 / 8;
        return new Color(r, g, b);
    }

    @Override
    public void onGameStateChanged() {
        refresh();
    }
}

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
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 44;

    private static final int GRID_ROWS = 3;
    private static final int GRID_COLS = 3;
    private static final int GAP = 4;

    private static final int TICKET_ROW_HEIGHT = 36;
    private static final int TICKET_ROW_SCROLL_COUNT = 4;

    public PlayerHandPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);

        setLayout(new BorderLayout(0, 6));
        setBorder(BorderFactory.createTitledBorder("Current Player's Hand"));
        setBackground(Color.WHITE);

        refresh();
    }

    //this is method that is run by the observer itself and it updates the UI
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

        JPanel trainCardGridPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, GAP, GAP));
        trainCardGridPanel.setBackground(new Color(248, 246, 238));
        trainCardGridPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        for (TrainCarCard.CardColor color : TrainCarCard.CardColor.values()) {
            int count = player.getPlayerhand().cardColorCount(color);
            trainCardGridPanel.add(makeCardTile(color, count));
        }

        add(trainCardGridPanel, BorderLayout.CENTER);

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

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(new Color(248, 246, 238));

        if (tickets.isEmpty()) {
            JLabel none = new JLabel("  (none yet)", JLabel.LEFT);
            none.setFont(new Font("Arial", Font.ITALIC, 11));
            none.setForeground(Color.GRAY);
            none.setMaximumSize(new Dimension(Integer.MAX_VALUE, TICKET_ROW_HEIGHT));
            none.setPreferredSize(new Dimension(200, TICKET_ROW_HEIGHT));
            list.add(none);
        } else {
            for (DestinationTicket ticket : tickets) {
                String statusIcon = ticket.isCompleted() ? " ✓" : "";
                JLabel lbl = new JLabel(
                        "  " + ticket.getCity1() + " ↔ " + ticket.getCity2()
                                + "  [" + ticket.getPoints() + " pts]" + statusIcon,
                        JLabel.LEFT);
                lbl.setFont(new Font("Arial", Font.PLAIN, 14));
                lbl.setOpaque(true);

                if (ticket.isCompleted()) {
                    lbl.setBackground(new Color(200, 235, 200));
                    lbl.setForeground(new Color(30, 100, 30));
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(80, 160, 80), 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                } else {
                    lbl.setBackground(new Color(255, 252, 230));
                    lbl.setForeground(Color.BLACK);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(200, 185, 120), 1),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
                }

                lbl.setPreferredSize(new Dimension(200, TICKET_ROW_HEIGHT));
                lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, TICKET_ROW_HEIGHT));
                lbl.setMinimumSize(new Dimension(50, TICKET_ROW_HEIGHT));
                list.add(lbl);
                list.add(Box.createVerticalStrut(2));
            }
        }

        int viewportH = TICKET_ROW_SCROLL_COUNT * (TICKET_ROW_HEIGHT + 2);

        JScrollPane scroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(248, 246, 238));
        scroll.setPreferredSize(new Dimension(200, viewportH));
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private Color cardColorToAwt(TrainCarCard.CardColor color) {
        switch (color) {
//            case RED:    return new Color(210, 60,  60);
            case BLUE:   return new Color(60,  110, 210);
            case GREEN:  return new Color(50,  160, 60);
            case YELLOW: return new Color(220, 190, 20);
            case BLACK:  return new Color(40,  40,  40);
//            case WHITE:  return new Color(220, 220, 220);
            case ORANGE: return new Color(220, 120, 30);
            case PINK:   return new Color(210, 100, 160);
            case LOCOMOTIVE:   return new Color(140, 80,  200);
            default:     return Color.GRAY;
        }
    }

    private Color textColorFor(TrainCarCard.CardColor color) {
        return (color == TrainCarCard.CardColor.YELLOW) ? Color.BLACK : Color.WHITE;
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
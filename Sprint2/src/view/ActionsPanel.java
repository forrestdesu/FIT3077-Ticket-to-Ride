package src.view;

import javax.swing.*;
import java.awt.*;

public class ActionsPanel extends JPanel {
    private final JButton claimRouteButton;
    private final JButton drawCardsButton;
    private final JButton drawTicketsButton;
    private final JButton endTurnButton;

    public ActionsPanel() {
        setLayout(new GridLayout(2, 2, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Actions"));
        claimRouteButton = new JButton("Claim Route");
        drawCardsButton = new JButton("Draw Cards");
        drawTicketsButton = new JButton("Draw Tickets");
        endTurnButton = new JButton("End Turn");

        endTurnButton.setBackground(new Color(180, 50, 50));
        endTurnButton.setForeground(Color.WHITE);
        endTurnButton.setOpaque(true);
        endTurnButton.setBorderPainted(false);
        endTurnButton.setFont(new Font("Arial", Font.BOLD, 12));

        add(claimRouteButton);
        add(drawCardsButton);
        add(drawTicketsButton);
        add(endTurnButton);
    }

    public JButton getClaimRouteButton() {
        return claimRouteButton;
    }

    public JButton getDrawCardsButton() {
        return drawCardsButton;
    }

    public JButton getDrawTicketsButton() {
        return drawTicketsButton;
    }

    public JButton getEndTurnButton() {
        return endTurnButton;
    }
}

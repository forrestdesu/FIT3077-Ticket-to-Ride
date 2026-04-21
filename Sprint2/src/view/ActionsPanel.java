package src.view;

import javax.swing.*;
import java.awt.*;

public class ActionsPanel extends JPanel {
    private final JButton claimRouteButton;
    private final JButton drawCardsButton;
    private final JButton drawTicketsButton;

    public ActionsPanel() {
        setLayout(new GridLayout(1, 3, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Actions"));
        claimRouteButton = new JButton("Claim Route");
        drawCardsButton = new JButton("Draw Cards");
        drawTicketsButton = new JButton("Draw Tickets");

        add(claimRouteButton);
        add(drawCardsButton);
        add(drawTicketsButton);
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
}

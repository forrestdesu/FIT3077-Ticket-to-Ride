package src.view;

import javax.swing.*;
import java.awt.*;

public class ActionsPanel extends JPanel {
    public ActionsPanel() {
        setLayout(new GridLayout(1, 3, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Actions"));
        add(new JButton("Claim Route"));
        add(new JButton("Draw Cards"));
        add(new JButton("Draw Tickets"));
    }
}

package src.view;

import javax.swing.*;
import java.awt.*;

public class DeckPanel extends JPanel {
    public DeckPanel() {
        setLayout(new GridLayout(1, 2, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Draw Decks"));
        add(new JButton("<html><center>Train Car<br>Deck</center></html>"));
        add(new JButton("<html><center>Destination<br>Deck</center></html>"));
    }
}

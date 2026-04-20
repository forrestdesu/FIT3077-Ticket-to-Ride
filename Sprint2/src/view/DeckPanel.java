package src.view;

import src.interfaces.GameObserver;
import src.model.GameEngine;

import javax.swing.*;
import java.awt.*;

public class DeckPanel extends JPanel implements GameObserver {
    private final GameEngine gameEngine;
    private final JButton trainCarDeckButton;
    private final JButton destinationDeckButton;

    public DeckPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        setLayout(new GridLayout(1, 2, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Draw Decks"));
        trainCarDeckButton = new JButton("<html><center>Train Car<br>Deck (" + gameEngine.getCardDeck().getDrawCardPile().size() + ")</center></html>");
        destinationDeckButton = new JButton("<html><center>Destination<br>Deck (" + gameEngine.getTicketDeck().ticketDeckSize() + ")</center></html>");

        add(trainCarDeckButton);
        add(destinationDeckButton);
    }

    public JButton getTrainCarDeckButton() {
        return trainCarDeckButton;
    }

    public JButton getDestinationDeckButton() {
        return destinationDeckButton;
    }

    private void refresh() {
        trainCarDeckButton.setText("<html><center>Train Car<br>Deck (" + gameEngine.getCardDeck().getDrawCardPile().size() + ")</center></html>");
        destinationDeckButton.setText("<html><center>Destination<br>Deck (" + gameEngine.getTicketDeck().ticketDeckSize() + ")</center></html>");
    }

    @Override
    public void onGameStateChange() {
        refresh();
    }
}

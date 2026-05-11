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

        // Register as observer so refresh() is called whenever game state changes
        gameEngine.addObserver(this);

        setLayout(new GridLayout(1, 2, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Draw Decks"));

        trainCarDeckButton = new JButton("");
        destinationDeckButton = new JButton("");

        add(trainCarDeckButton);
        add(destinationDeckButton);

        refresh(); // set initial button text with correct counts
    }

    public JButton getTrainCarDeckButton() {
        return trainCarDeckButton;
    }

    public JButton getDestinationDeckButton() {
        return destinationDeckButton;
    }

    //this is method that is run by the observer itself and it updates the UI
    private void refresh() {
        trainCarDeckButton.setText(
                "<html><center>Train Car Card<br>Deck ("
                        + gameEngine.getCardDeck().getDrawCardPile().size()
                        + ")</center></html>");
        destinationDeckButton.setText(
                "<html><center>Destination Tickets<br>Deck ("
                        + gameEngine.getTicketDeck().ticketDeckSize()
                        + ")</center></html>");
    }

    @Override
    public void onGameStateChanged() {
        refresh();
    }
}
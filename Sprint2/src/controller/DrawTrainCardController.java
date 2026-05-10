package src.controller;

import src.exception.TrainCardDrawError;
import src.model.CardDeck;
import src.model.GameEngine;
import src.model.Player;
import src.model.TrainCarCard;
import src.view.MainFrame;

import javax.swing.*;
import java.util.List;
public class DrawTrainCardController {

    private static final int MAX_DRAWS_PER_TURN = 2;

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;
    private final TurnController turnController;

    private int drawsUsedThisTurn;
    private boolean drawModeActive;

    public DrawTrainCardController(MainFrame mainFrame, GameEngine gameEngine,
                                   TurnController turnController) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.turnController = turnController;
        this.drawsUsedThisTurn = 0;
        this.drawModeActive = false;
        wireListeners();
    }

    private void wireListeners() {
        mainFrame.getDrawCardsButton().addActionListener(e -> handleEnterDrawMode());

        for (int i = 0; i < GameEngine.FACE_UP_CARD_COUNT; i++) {
            final int index = i;
            mainFrame.getFaceUpButton(index).addActionListener(e -> handleFaceUpCardClick(index));
        }

        mainFrame.getTrainCarDeckButton().addActionListener(e -> handleBlindDraw());
    }

    /**
     * Called when the player clicks "Draw Cards".
     * Locks all other action buttons immediately via onActionStarted().
     */
    private void handleEnterDrawMode() {
        if (drawModeActive) {
            return;
        }

        CardDeck deck = gameEngine.getCardDeck();
        List<TrainCarCard> faceUp = gameEngine.getFaceUpCards();

        if (deck.isCardDeckEmpty() && faceUp.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "No train car cards are available to draw. Please choose a different action.",
                    "No Cards Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        drawModeActive = true;
        drawsUsedThisTurn = 0;

        // Do NOT lock buttons yet — player can still change their mind until they
        // actually draw their first card. Buttons are locked after the first draw.
        mainFrame.setTitle("London Map - Draw a card (click a face-up card or the deck)");
    }

    /**
     * Handles the player clicking one of the 5 face-up card buttons.
     *
     * @param index the 0-based index of the face-up card slot
     */
    private void handleFaceUpCardClick(int index) {
        if (!drawModeActive) {
            return;
        }

        List<TrainCarCard> faceUp = gameEngine.getFaceUpCards();

        if (index >= faceUp.size()) {
            return;
        }

        TrainCarCard selectedCard = faceUp.get(index);

        try {
            validateFaceUpDraw(selectedCard);
        } catch (TrainCardDrawError e) {
            JOptionPane.showMessageDialog(
                    mainFrame, e.getMessage(), "Invalid Draw", JOptionPane.WARNING_MESSAGE);
            return;
        }

        faceUp.remove(index);
        Player currentPlayer = gameEngine.getCurrentPlayer();
        currentPlayer.getPlayerhand().addCard(selectedCard);

        // Lock other action buttons now that the player has committed to drawing
        if (drawsUsedThisTurn == 0) {
            turnController.onActionStarted();
        }

        gameEngine.refillFaceUpCards();
        drawsUsedThisTurn++;

        boolean isLocomotive = selectedCard.isLocomotive();
        if (isLocomotive || drawsUsedThisTurn >= MAX_DRAWS_PER_TURN) {
            endDrawPhase();
        } else {
            gameEngine.notifyObservers();
            mainFrame.setTitle("London Map - Draw your second card");
        }
    }

    /**
     * Handles the player clicking the blind deck button.
     * A Locomotive drawn blindly still allows a second draw.
     */
    private void handleBlindDraw() {
        if (!drawModeActive) {
            return;
        }

        CardDeck deck = gameEngine.getCardDeck();

        if (deck.isCardDeckEmpty()) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "The draw deck is empty. Please draw a face-up card instead.",
                    "Deck Empty",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        TrainCarCard drawnCard = deck.drawCard();
        if (drawnCard == null) {
            return;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        currentPlayer.getPlayerhand().addCard(drawnCard);

        // Lock other action buttons now that the player has committed to drawing
        if (drawsUsedThisTurn == 0) {
            turnController.onActionStarted();
        }

        drawsUsedThisTurn++;

        if (drawsUsedThisTurn >= MAX_DRAWS_PER_TURN) {
            endDrawPhase();
        } else {
            gameEngine.notifyObservers();
            mainFrame.setTitle("London Map - Draw your second card");
        }
    }

    /** Validates whether a face-up card can legally be drawn.*/
    private void validateFaceUpDraw(TrainCarCard card) throws TrainCardDrawError {
        if (drawsUsedThisTurn == 1 && card.isLocomotive()) {
            throw new TrainCardDrawError(
                    "You cannot take a face-up Locomotive as your second card."
                            + " Draw from the deck or pick a regular card.");
        }
    }

    /**
     * Cancels draw mode without locking buttons or completing the action.
     * Called when the player clicks a different action button after entering draw mode.
     */
    public void cancelDrawMode() {
        drawModeActive = false;
        drawsUsedThisTurn = 0;
        mainFrame.setTitle("London Map - Game On");
    }

    /**
     * Ends the draw phase and signals TurnController that the action is complete.
     */
    private void endDrawPhase() {
        drawModeActive = false;
        drawsUsedThisTurn = 0;
        gameEngine.notifyObservers();
        turnController.onActionCompleted();
    }
}
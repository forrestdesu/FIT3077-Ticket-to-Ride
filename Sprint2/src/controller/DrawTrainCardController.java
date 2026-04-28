package src.controller;

import src.exception.TrainCardDrawError;
import src.model.CardDeck;
import src.model.GameEngine;
import src.model.Player;
import src.model.TrainCarCard;
import src.view.MainFrame;

import javax.swing.*;
import java.util.List;

/**
 * Controller responsible for handling the "Draw Train Car Cards" action.
 *
 * <p>A player may draw up to 2 train car cards per turn by either:
 * <ul>
 *   <li>Clicking a face-up card to take it from the face-up row, or</li>
 *   <li>Clicking the blind deck button to draw the top card unseen.</li>
 * </ul>
 *
 * <p>Locomotive rules enforced:
 * <ul>
 *   <li>Taking a face-up Locomotive costs the entire draw phase (only 1 card drawn).</li>
 *   <li>A face-up Locomotive cannot be taken as the 2nd card.</li>
 *   <li>A Locomotive drawn blindly still counts as 1 of the 2 draws.</li>
 * </ul>
 */
public class DrawTrainCardController {

    private static final int MAX_DRAWS_PER_TURN = 2;

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;

    /** Tracks how many cards the current player has drawn this turn (0, 1, or 2). */
    private int drawsUsedThisTurn;

    /** Whether the player has clicked "Draw Cards" and is in an active drawing phase. */
    private boolean drawModeActive;

    /**
     * Constructs the controller and wires all button listeners.
     *
     * @param mainFrame  the main game window
     * @param gameEngine the central game state
     */
    public DrawTrainCardController(MainFrame mainFrame, GameEngine gameEngine) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.drawsUsedThisTurn = 0;
        this.drawModeActive = false;

        wireListeners();
    }

    /** Attaches action listeners to the Draw Cards button, all 5 face-up slots, and the blind deck. */
    private void wireListeners() {
        // "Draw Cards" action button — enters draw mode
        mainFrame.getDrawCardsButton().addActionListener(e -> handleEnterDrawMode());

        // Each of the 5 face-up card buttons
        for (int i = 0; i < GameEngine.FACE_UP_CARD_COUNT; i++) {
            final int index = i;
            mainFrame.getFaceUpButton(index).addActionListener(e -> handleFaceUpCardClick(index));
        }

        // Blind draw from the top of the deck
        mainFrame.getTrainCarDeckButton().addActionListener(e -> handleBlindDraw());
    }

    /**
     * Called when the player clicks "Draw Cards".
     * Validates that drawing is possible and activates draw mode.
     */
    private void handleEnterDrawMode() {
        if (drawModeActive) {
            return; // already in draw mode, ignore double-click
        }

        CardDeck deck = gameEngine.getCardDeck();
        List<TrainCarCard> faceUp = gameEngine.getFaceUpCards();

        // Edge case: no cards available anywhere
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
        // Visual feedback: update title so player knows draw mode is active
        mainFrame.setTitle("London Map - Draw a card (click a face-up card or the deck)");
    }

    /**
     * Handles the player clicking one of the 5 face-up card buttons.
     *
     * @param index the 0-based index of the face-up card slot
     */
    private void handleFaceUpCardClick(int index) {
        if (!drawModeActive) {
            return; // ignore clicks outside of draw mode
        }

        List<TrainCarCard> faceUp = gameEngine.getFaceUpCards();

        if (index >= faceUp.size()) {
            return; // slot is empty, ignore
        }

        TrainCarCard selectedCard = faceUp.get(index);

        try {
            validateFaceUpDraw(selectedCard);
        } catch (TrainCardDrawError e) {
            JOptionPane.showMessageDialog(
                    mainFrame, e.getMessage(), "Invalid Draw", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Remove from face-up and add to player hand
        faceUp.remove(index);
        Player currentPlayer = gameEngine.getCurrentPlayer();
        currentPlayer.getPlayerhand().addCard(selectedCard);

        // Refill the face-up row from the deck
        gameEngine.refillFaceUpCards();
        drawsUsedThisTurn++;

        // A face-up Locomotive costs the full draw phase
        boolean isLocomotive = selectedCard.isLocomotive();
        if (isLocomotive || drawsUsedThisTurn >= MAX_DRAWS_PER_TURN) {
            endDrawPhase();
        } else {
            // Notify observers to refresh UI (hand, face-up cards) mid-draw
            gameEngine.notifyObservers();
        }
    }

    /**
     * Handles the player clicking the blind deck button to draw the top card unseen.
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
            return; // defensive check
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        currentPlayer.getPlayerhand().addCard(drawnCard);
        drawsUsedThisTurn++;

        if (drawsUsedThisTurn >= MAX_DRAWS_PER_TURN) {
            endDrawPhase();
        } else {
            // Notify observers to refresh hand count mid-draw
            gameEngine.notifyObservers();
        }
    }

    /**
     * Validates whether a face-up card can legally be drawn given the current draw state.
     *
     * @param card the face-up card the player wants to take
     * @throws TrainCardDrawError if the draw is illegal
     */
    private void validateFaceUpDraw(TrainCarCard card) throws TrainCardDrawError {
        if (drawsUsedThisTurn == 1 && card.isLocomotive()) {
            throw new TrainCardDrawError(
                    "You cannot take a face-up Locomotive as your second card."
                            + " Draw from the deck or pick a regular card.");
        }
    }

    /**
     * Ends the draw phase: resets state, advances the turn, and notifies the UI.
     * Called after the player has used both draws, or drew a face-up Locomotive.
     */
    private void endDrawPhase() {
        drawModeActive = false;
        drawsUsedThisTurn = 0;
        mainFrame.setTitle("London Map - Game On");
        gameEngine.nextPlayerTurn();
    }
}
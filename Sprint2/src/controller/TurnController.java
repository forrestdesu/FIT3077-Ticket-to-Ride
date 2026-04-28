package src.controller;

import src.model.GameEngine;
import src.view.MainFrame;

import javax.swing.*;

/**
 * Controller responsible for managing turn progression.
 *
 * <p>Per the rulebook, a player MUST perform exactly one action per turn
 * (draw train cards, draw destination tickets, or claim a route). A player
 * cannot end their turn without completing an action.
 *
 * <p>Other controllers call {@link #onActionCompleted()} when their action
 * finishes. This disables all three action buttons so no further actions
 * can be taken, and enables the End Turn button. Pressing End Turn shows a
 * confirmation dialog — if confirmed, the turn advances and action buttons
 * are re-enabled for the next player.
 */
public class TurnController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;

    /** Whether the current player has completed a valid action this turn. */
    private boolean actionCompletedThisTurn;

    /**
     * Constructs the TurnController and sets the initial button states.
     *
     * @param mainFrame  the main game window
     * @param gameEngine the central game state
     */
    public TurnController(MainFrame mainFrame, GameEngine gameEngine) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.actionCompletedThisTurn = false;

        // End Turn starts disabled — player must complete an action first
        mainFrame.getEndTurnButton().setEnabled(false);

        mainFrame.getEndTurnButton().addActionListener(e -> handleEndTurn());
    }

    /**
     * Called by action controllers when the player has successfully completed
     * their action for this turn. Disables all action buttons to prevent a
     * second action being taken, and enables the End Turn button.
     */
    public void onActionCompleted() {
        actionCompletedThisTurn = true;

        // Lock all action buttons — only one action allowed per turn
        mainFrame.getDrawCardsButton().setEnabled(false);
        mainFrame.getDrawTicketsButton().setEnabled(false);
        mainFrame.getClaimRouteButton().setEnabled(false);

        // Allow the player to end their turn
        mainFrame.getEndTurnButton().setEnabled(true);
        mainFrame.setTitle("London Map - Action done! Press End Turn when ready.");
    }

    /**
     * Called when the player clicks the End Turn button.
     * Shows a Yes/No confirmation dialog. If confirmed, the turn advances.
     * If the player has not yet completed an action, shows a warning instead.
     */
    private void handleEndTurn() {
        if (!actionCompletedThisTurn) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "You must perform an action before ending your turn.\n"
                            + "Choose one of: Draw Cards, Draw Tickets, or Claim Route.",
                    "Action Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(
                mainFrame,
                "Are you sure you want to end your turn?",
                "End Turn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            endTurn();
        }
        // NO_OPTION: do nothing, player stays in their turn
    }

    /**
     * Advances the turn to the next player and resets all button states
     * ready for the next player's turn.
     */
    private void endTurn() {
        actionCompletedThisTurn = false;

        // Re-enable action buttons for the next player
        mainFrame.getDrawCardsButton().setEnabled(true);
        mainFrame.getDrawTicketsButton().setEnabled(true);
        mainFrame.getClaimRouteButton().setEnabled(true);

        mainFrame.getEndTurnButton().setEnabled(false);
        mainFrame.setTitle("London Map - Game On");
        gameEngine.nextPlayerTurn();
    }
}
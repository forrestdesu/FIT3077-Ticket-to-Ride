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
 * finishes. Only then is the End Turn button enabled. Pressing End Turn
 * shows a confirmation dialog — if confirmed, the turn advances.
 */
public class TurnController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;

    /** Whether the current player has completed a valid action this turn. */
    private boolean actionCompletedThisTurn;

    /**
     * Constructs the TurnController and wires the End Turn button.
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
     * Called by other controllers (DrawTrainCardController, DrawTicketController,
     * ClaimRouteController) when the player has successfully completed their action.
     * Enables the End Turn button so the player can manually advance the turn.
     */
    public void onActionCompleted() {
        actionCompletedThisTurn = true;
        mainFrame.getEndTurnButton().setEnabled(true);
        mainFrame.setTitle("London Map - Action done! Press End Turn when ready.");
    }

    /**
     * Called when the player clicks the End Turn button.
     * Shows a confirmation dialog. If confirmed, advances the turn and resets state.
     * If the player has not completed an action, shows a warning instead.
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
        // NO_OPTION: do nothing, player returns to the game
    }

    /**
     * Advances the turn to the next player and resets all turn state.
     */
    private void endTurn() {
        actionCompletedThisTurn = false;
        mainFrame.getEndTurnButton().setEnabled(false);
        mainFrame.setTitle("London Map - Game On");
        gameEngine.nextPlayerTurn();
    }
}
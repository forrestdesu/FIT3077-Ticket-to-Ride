package src.controller;

import src.model.GameEngine;
import src.view.GameStart;
import src.view.MainFrame;

import javax.swing.*;

public class TurnController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;

    private boolean actionCompletedThisTurn;

    public TurnController(MainFrame mainFrame, GameEngine gameEngine) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.actionCompletedThisTurn = false;

        mainFrame.getEndTurnButton().setEnabled(false);
        mainFrame.getEndTurnButton().addActionListener(e -> handleEndTurn());
    }

    /**
     * Called by action controllers as soon as the player commits to an action.
     * Immediately locks all other action buttons so the player cannot switch
     * to a different action mid-turn.
     */
    public void onActionStarted() {
        mainFrame.getDrawCardsButton().setEnabled(false);
        mainFrame.getDrawTicketsButton().setEnabled(false);
        mainFrame.getClaimRouteButton().setEnabled(false);
    }

    /**
     * Called by action controllers when the player has successfully completed
     * their action for this turn. Enables the End Turn button.
     */
    public void onActionCompleted() {
        actionCompletedThisTurn = true;
        mainFrame.getDrawCardsButton().setEnabled(false);
        mainFrame.getDrawTicketsButton().setEnabled(false);
        mainFrame.getClaimRouteButton().setEnabled(false);
        mainFrame.getEndTurnButton().setEnabled(true);
        mainFrame.setTitle("London Map - Action done! Press End Turn when ready.");
    }

    /**
     * Called when the player clicks the End Turn button.
     * Shows a Yes/No confirmation dialog. If confirmed, the turn advances.
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
    }

    /**
     * Advances the turn to the next player, resets button states, then checks
     * for end-game and final-turn conditions.
     */
    public void endTurn() {
        actionCompletedThisTurn = false;

        // Advance the turn inside the game engine (also sets gameOver flag if needed)
        gameEngine.nextPlayerTurn();

        // --- Check: is the game now over? ---
        if (gameEngine.isGameOver()) {
            showFinalScores();
            return; // Do not re-enable buttons; game is done
        }

        // Re-enable action buttons for the next player's turn
        mainFrame.getDrawCardsButton().setEnabled(true);
        mainFrame.getDrawTicketsButton().setEnabled(true);
        mainFrame.getClaimRouteButton().setEnabled(true);
        mainFrame.getEndTurnButton().setEnabled(false);
        mainFrame.setTitle("London Map - Game On");

        // --- Check: is the next player about to take their final turn? ---
        if (gameEngine.isCurrentPlayerOnFinalTurn()) {
            String playerName = gameEngine.getCurrentPlayer().getPlayerName();
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "⚠️  FINAL TURN!\n\n"
                            + playerName + ", this is your last turn.\n"
                            + "Make it count — the game ends after you complete your action!",
                    "Final Turn — " + playerName,
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Applies final scoring, builds a score summary, and shows it in a dialog.
     * After the player dismisses the dialog the application exits cleanly.
     */
    private void showFinalScores() {
        // Disable all action buttons — game is over
        mainFrame.getDrawCardsButton().setEnabled(false);
        mainFrame.getDrawTicketsButton().setEnabled(false);
        mainFrame.getClaimRouteButton().setEnabled(false);
        mainFrame.getEndTurnButton().setEnabled(false);
        mainFrame.setTitle("London Map - Game Over!");

        // Notify observers so the board re-paints with final state
        gameEngine.notifyObservers();

        // Build summary (also applies score adjustments exactly once)
        String summary = gameEngine.applyAndBuildFinalScoreSummary();

        String message = summary + "\n\nWould you like to start a new game?";

        // Notify observers again so score panel reflects the adjusted totals
        gameEngine.notifyObservers();

        int response = JOptionPane.showConfirmDialog(
                mainFrame,
                message,
                "Game Over — Final Scores",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            new GameStart();
        } else {
            // If they click 'No' or close the dialog, exit the app
            System.exit(0);
        }
    }
}
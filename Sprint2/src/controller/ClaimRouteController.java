package src.controller;

import src.model.GameEngine;
import src.view.MainFrame;
import src.model.Route;

import javax.swing.*;

/**
 *
 * controller handling ClaimRoute user action
 *
 * using the commandpattern:
 *- when the player wants to claim a route, create a ClaimRouteCommand object that bundles
 * all details (who, which route, which cards)
 *
 *- command validates itself checking all rules -if valid, command executes itself (modifies gamestate)
 *      - executed command is stored in ClaimHistory
 *
 * from players perspective:
 * first click on  "Claim Route" button, system enters claim mode
 * then click a route on board, system selects it
 * then click "Claim Route" again to attempt the claim
 * if valid, and route is claimed, score updates, turn ends
 * if invalid -> show error popup, player can try again
 *
 */

public class ClaimRouteController {

    // reference main game window (to read UI state)
    private final MainFrame mainFrame;

    // to get current player, card deck so on
    private final GameEngine gameEngine;

    // signal "action complete" after a claim
    private final TurnController turnController;

    // command pattern: to store executed commands
    private final ClaimHistory claimHistory;

    // track if the player is currently in "claim mode"
    private boolean claimModeActive;

    /**
     * create claim route controller and set up button listeners
     *
     * turnController -to notify when done
     */
    public ClaimRouteController(MainFrame mainFrame, GameEngine gameEngine, TurnController turnController) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.turnController = turnController;
        this.claimHistory = new ClaimHistory();
        this.claimModeActive = false;
        wireListeners(); // click listener on the "Claim Route" button
    }


    private void wireListeners() {
        mainFrame.getClaimRouteButton().addActionListener(e -> handleClaimRouteClick());
    }

    /**
     *
     * first click to enter claim mode the player then can click a route on the board
     * they click one more time to attempt to claim the selected route ----  TO DO
     */
    private void handleClaimRouteClick() {
        // if not in claim mode yet - enter it
        if (!claimModeActive) {
            claimModeActive = true; // Update the title bar to tell the player what to do next
            mainFrame.setTitle(
                    "London Map - Click a route on the board, then press Claim Route again");
            return;
        }

        // secondclick: read which route the player clicked on the board
        // "BoardPanel" to track which route was last clicked by the player
        Route selectedRoute = mainFrame.getBoardPanel().getSelectedRoute();

        // if the player hasn't clicked any route yet, show message
        if (selectedRoute == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Please click a route on the board first.",
                    "No Route Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // we have a selected route
        System.out.println("Route selected: " + selectedRoute.getCity1().getName()
                + " to " + selectedRoute.getCity2().getName()
                + " (length " + selectedRoute.getRouteLength() + ")");
    }
}

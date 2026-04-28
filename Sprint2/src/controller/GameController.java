package src.controller;

import src.model.GameEngine;
import src.view.MainFrame;

/**
 * Top-level controller that instantiates and wires all action sub-controllers.
 *
 * <p>MainFrame creates this once after building all UI panels. Each sub-controller
 * attaches its own listeners to the relevant buttons.
 */
public class GameController {

    private final DrawTrainCardController drawTrainCardController;
    private final DrawTicketController drawTicketController;

    /**
     * Constructs the GameController and wires all sub-controllers.
     *
     * @param mainFrame  the main game window
     * @param gameEngine the central game state
     */
    public GameController(MainFrame mainFrame, GameEngine gameEngine) {
        drawTrainCardController = new DrawTrainCardController(mainFrame, gameEngine);
        drawTicketController = new DrawTicketController(mainFrame, gameEngine);
        // ClaimRouteController will be added in a subsequent commit
    }
}
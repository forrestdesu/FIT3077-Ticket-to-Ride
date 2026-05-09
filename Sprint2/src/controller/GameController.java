package src.controller;

import src.model.GameEngine;
import src.view.MainFrame;

/**
 * Top-level controller that instantiates and wires all action sub-controllers.
 *
 * <p>TurnController is created first as it is passed into each action controller
 * so they can notify it when their action completes, enabling the End Turn button.
 */
public class GameController {

    private final TurnController turnController;
    private final DrawTrainCardController drawTrainCardController;
    private final DrawTicketController drawTicketController;
    private final ClaimRouteController claimRouteController;

    /**
     * Constructs the GameController and wires all sub-controllers.
     *
     * @param mainFrame  the main game window
     * @param gameEngine the central game state
     */
    public GameController(MainFrame mainFrame, GameEngine gameEngine) {
        turnController = new TurnController(mainFrame, gameEngine);
        drawTrainCardController = new DrawTrainCardController(mainFrame, gameEngine, turnController);
        drawTicketController = new DrawTicketController(mainFrame, gameEngine, turnController);
        claimRouteController = new ClaimRouteController(mainFrame, gameEngine, turnController);
    }
}
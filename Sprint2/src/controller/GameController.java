package src.controller;

import src.model.GameEngine;
import src.view.MainFrame;

public class GameController {

    private final TurnController turnController;
    private final DrawTrainCardController drawTrainCardController;
    private final DrawTicketController drawTicketController;
    private final ClaimRouteController claimRouteController;

    public GameController(MainFrame mainFrame, GameEngine gameEngine) {
        turnController = new TurnController(mainFrame, gameEngine);
        drawTrainCardController = new DrawTrainCardController(mainFrame, gameEngine, turnController);
        drawTicketController = new DrawTicketController(mainFrame, gameEngine, turnController);
        claimRouteController = new ClaimRouteController(mainFrame, gameEngine, turnController);

        // If the player entered draw-cards mode but hasn't drawn yet,
        // clicking Draw Tickets or Claim Route cancels draw mode first.
        mainFrame.getDrawTicketsButton().addActionListener(
                e -> drawTrainCardController.cancelDrawMode());
        mainFrame.getClaimRouteButton().addActionListener(
                e -> drawTrainCardController.cancelDrawMode());
    }
}
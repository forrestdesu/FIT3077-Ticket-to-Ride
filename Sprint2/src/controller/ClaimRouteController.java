package src.controller;

import src.exception.InsufficientTrainCar;
import src.exception.RouteAlreadyClaimed;
import src.model.*;
import src.view.MainFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


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
 * command pattern flow --------
 * First - create the command:   new ClaimRouteCommand(player, route, colour)
 * second - validate it:      command.validate()
 * third - execute it:    command.execute(cardDeck)
 * forth - Store it:    claimHistory.recordClaim(command)
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

        Player currentPlayer = gameEngine.getCurrentPlayer();

        // COMMAND PATTERN 1ststep: Determine the card colour
        TrainCarCard.CardColor colorToUse = determineClaimColor(currentPlayer, selectedRoute);

        if (colorToUse == null) {
            // player can't afford it or cancelled the grey route dialog
            JOptionPane.showMessageDialog(mainFrame,
                    "You don't have enough matching cards to claim this route.",
                    "Cannot Claim Route", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // COMMAND PATTERN 2nd step: Create the command
        ClaimRouteCommand command = new ClaimRouteCommand(
                currentPlayer, selectedRoute, colorToUse);

        // COMMAND PATTERN 3rd step: Validate the command

        // The command checks its own preconditions -if anything is wrong,
        // throw an exception with error message
        try {
            command.validate();
        } catch (RouteAlreadyClaimed | InsufficientTrainCar e) {
            JOptionPane.showMessageDialog(mainFrame,
                    e.getMessage(), "Cannot Claim Route", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // if Validation passed — TO DO
    }

        // we have a selected route
        //System.out.println("Route selected: " + selectedRoute.getCity1().getName()
        //        + " to " + selectedRoute.getCity2().getName()
         //       + " (length " + selectedRoute.getRouteLength() + ")");
    }
    /**
     * determines which card colour the player will use to claim the route
     *
     * for coloured routes: the colour is fixed (must use that colour)
     * for grey routes: shows a dialog letting the player choose
     *
     * @return the chosen colour, or null if player can't afford / cancelled
     */
    private TrainCarCard.CardColor determineClaimColor(Player player, Route route) {
        List<TrainCarCard.CardColor> validColors = findValidColors(player, route);

        // if no colours work- player can't afford this route
        if (validColors.isEmpty()) {
            return null;
        }

        // for coloured routes: only one option, use it directly
        if (route.getRouteColor() != Route.RouteColor.GREY) {
            return validColors.get(0);
        }

        // for grey routes: let the player pick from valid colours
        return showGreyRouteColorChooser(validColors, player);
    }

    /**
     * shows a dialog that lets the player choose which card colour to spend
     * when claiming a grey route
     *
     * grey routes can be claimed with any single colour, so ask for colour
     *
     * only shows colours the player can actually afford
     * if only one colour works, skips dialog
     *
     * @return the chosen colour, or null if player pressed Cancel
     */
    private TrainCarCard.CardColor showGreyRouteColorChooser(
            List<TrainCarCard.CardColor> validColors, Player player) {

        // if only one colour works, no need to ask
        if (validColors.size() == 1) {
            return validColors.get(0);
        }

        // build the list of options like BLUE (3 cards), RED (2 cards) and so on
        String[] options = new String[validColors.size()];
        for (int i = 0; i < validColors.size(); i++) {
            TrainCarCard.CardColor color = validColors.get(i);
            int count = player.getPlayerhand().cardColorCount(color);
            options[i] = color.name() + " (" + count + " cards)";
        }

        // show a dialog with buttons for each valid colour
        int choice = JOptionPane.showOptionDialog(
                mainFrame,
                "This is a grey route — you can use any colour.\n"
                        + "Choose which card colour to spend:",
                "Choose Card Colour",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        // -1 means the player closed the dialog or pressed Cancel
        if (choice < 0) {
            return null;
        }

        return validColors.get(choice);
    }


    /**
     * converts a Route.RouteColor to the matching TrainCarCard.CardColor
     *
     * routes and cards use different enums, so need mapping
     *
     * returns null for GREY routes (player must choose which colour to use
     */
    private TrainCarCard.CardColor routeColorToCardColor(Route.RouteColor routeColor) {
        switch (routeColor) {
            case RED:    return TrainCarCard.CardColor.RED;
            case BLUE:   return TrainCarCard.CardColor.BLUE;
            case GREEN:  return TrainCarCard.CardColor.GREEN;
            case YELLOW: return TrainCarCard.CardColor.YELLOW;
            case BLACK:  return TrainCarCard.CardColor.BLACK;
            case WHITE:  return TrainCarCard.CardColor.WHITE;
            case ORANGE: return TrainCarCard.CardColor.ORANGE;
            case PINK:   return TrainCarCard.CardColor.PINK;
            default:     return null; //  player must choose
        }
    }

    /**
     * finds all card colours that the player could legally use to claim
     * the given route. A colour is "valid" if:
     *   cards of that colour + locomotive wild cards >= route length
     *
     * for coloured routes: only one valid colour (the route's colour).
     * for grey routes: any colour the player has enough of.
     */
    private List<TrainCarCard.CardColor> findValidColors(Player player, Route route) {
        List<TrainCarCard.CardColor> validColors = new ArrayList<>();
        int length = route.getRouteLength();
        int locoCount = player.getPlayerhand().locomotiveCount();

        if (route.getRouteColor() != Route.RouteColor.GREY) {
            // coloured route: only one possible colour
            TrainCarCard.CardColor required = routeColorToCardColor(route.getRouteColor());
            int colorCount = player.getPlayerhand().cardColorCount(required);
            if (colorCount + locoCount >= length) {
                validColors.add(required);
            }
        } else {
            // grey route: check every colour to see which ones work
            for (TrainCarCard.CardColor color : TrainCarCard.CardColor.values()) {
                if (color == TrainCarCard.CardColor.LOCOMOTIVE) continue;
                int colorCount = player.getPlayerhand().cardColorCount(color);
                if (colorCount + locoCount >= length) {
                    validColors.add(color);
                }
            }
            // also allow paying entirely with locomotives
            if (locoCount >= length) {
                validColors.add(TrainCarCard.CardColor.LOCOMOTIVE);
            }
        }

        return validColors;
    }
}




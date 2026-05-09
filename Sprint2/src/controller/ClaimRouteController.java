package src.controller;

import src.exception.InsufficientTrainCar;
import src.exception.RouteAlreadyClaimed;
import src.model.*;
import src.util.TicketCompletionChecker;
import src.view.MainFrame;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimRouteController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;
    private final TurnController turnController;
    private final ClaimHistory claimHistory;
    private boolean claimModeActive;

    public ClaimRouteController(MainFrame mainFrame, GameEngine gameEngine,
                                TurnController turnController) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.turnController = turnController;
        this.claimHistory = new ClaimHistory();
        this.claimModeActive = false;
        wireListeners();
    }

    private void wireListeners() {
        mainFrame.getClaimRouteButton().addActionListener(e -> handleClaimRouteClick());
    }

    private void handleClaimRouteClick() {
        if (!claimModeActive) {
            claimModeActive = true;
            mainFrame.setTitle(
                    "London Map - Click a route on the board, then press Claim Route again");
            return;
        }

        Route selectedRoute = mainFrame.getBoardPanel().getSelectedRoute();
        if (selectedRoute == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Please click a route on the board first.",
                    "No Route Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();

        TrainCarCard.CardColor colorToUse = determineClaimColor(currentPlayer, selectedRoute);
        if (colorToUse == null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "You don't have enough matching cards to claim this route.",
                    "Cannot Claim Route", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClaimRouteCommand command = new ClaimRouteCommand(
                currentPlayer, selectedRoute, colorToUse);

        try {
            command.validate();
        } catch (RouteAlreadyClaimed | InsufficientTrainCar e) {
            JOptionPane.showMessageDialog(mainFrame,
                    e.getMessage(), "Cannot Claim Route", JOptionPane.WARNING_MESSAGE);
            return;
        }

        command.execute(gameEngine.getCardDeck());
        claimHistory.recordClaim(command);

        List<DestinationTicket> completedTickets = checkTicketCompletion(currentPlayer);

        if (!completedTickets.isEmpty()) {
            StringBuilder message = new StringBuilder("Destination Ticket completed!\n\n");
            for (DestinationTicket ticket : completedTickets) {
                message.append("✓ ").append(ticket.getCity1()).append(" ↔ ")
                        .append(ticket.getCity2())
                        .append(" (+").append(ticket.getPoints()).append(" points)\n");
            }
            JOptionPane.showMessageDialog(mainFrame, message.toString(),
                    "Ticket Completed!", JOptionPane.INFORMATION_MESSAGE);
        }

        mainFrame.getBoardPanel().clearSelection();
        claimModeActive = false;
        mainFrame.setTitle("London Map - Game On");
        gameEngine.notifyObservers();
        turnController.onActionCompleted();
    }

    private List<DestinationTicket> checkTicketCompletion(Player player) {
        List<DestinationTicket> newlyCompleted = new ArrayList<>();
        for (DestinationTicket ticket : player.getPlayerTickets()) {
            if (ticket.isCompleted()) {
                continue;
            }
            if (TicketCompletionChecker.isTicketCompleted(player, ticket)) {
                ticket.setCompleted(true);
                player.adjustPlayerScore(ticket.getPoints());
                newlyCompleted.add(ticket);
            }
        }
        return newlyCompleted;
    }

    private TrainCarCard.CardColor determineClaimColor(Player player, Route route) {
        List<TrainCarCard.CardColor> validColors = findValidColors(player, route);
        if (validColors.isEmpty()) {
            return null;
        }
        if (route.getRouteColor() != Route.RouteColor.GREY) {
            return validColors.get(0);
        }
        return showGreyRouteColorChooser(validColors, player);
    }

    private TrainCarCard.CardColor showGreyRouteColorChooser(
            List<TrainCarCard.CardColor> validColors, Player player) {
        if (validColors.size() == 1) {
            return validColors.get(0);
        }
        String[] options = new String[validColors.size()];
        for (int i = 0; i < validColors.size(); i++) {
            TrainCarCard.CardColor color = validColors.get(i);
            int count = player.getPlayerhand().cardColorCount(color);
            options[i] = color.name() + " (" + count + " cards)";
        }
        int choice = JOptionPane.showOptionDialog(mainFrame,
                "This is a grey route — you can use any colour.\nChoose which card colour to spend:",
                "Choose Card Colour", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice < 0) {
            return null;
        }
        return validColors.get(choice);
    }

    private TrainCarCard.CardColor routeColorToCardColor(Route.RouteColor routeColor) {
        switch (routeColor) {
            case RED: return TrainCarCard.CardColor.RED;
            case BLUE: return TrainCarCard.CardColor.BLUE;
            case GREEN: return TrainCarCard.CardColor.GREEN;
            case YELLOW: return TrainCarCard.CardColor.YELLOW;
            case BLACK: return TrainCarCard.CardColor.BLACK;
            case WHITE: return TrainCarCard.CardColor.WHITE;
            case ORANGE: return TrainCarCard.CardColor.ORANGE;
            case PINK: return TrainCarCard.CardColor.PINK;
            default: return null;
        }
    }

    private List<TrainCarCard.CardColor> findValidColors(Player player, Route route) {
        List<TrainCarCard.CardColor> validColors = new ArrayList<>();
        int length = route.getRouteLength();
        int locoCount = player.getPlayerhand().locomotiveCount();
        if (route.getRouteColor() != Route.RouteColor.GREY) {
            TrainCarCard.CardColor required = routeColorToCardColor(route.getRouteColor());
            int colorCount = player.getPlayerhand().cardColorCount(required);
            if (colorCount + locoCount >= length) {
                validColors.add(required);
            }
        } else {
            for (TrainCarCard.CardColor color : TrainCarCard.CardColor.values()) {
                if (color == TrainCarCard.CardColor.LOCOMOTIVE) continue;
                int colorCount = player.getPlayerhand().cardColorCount(color);
                if (colorCount + locoCount >= length) {
                    validColors.add(color);
                }
            }
            if (locoCount >= length) {
                validColors.add(TrainCarCard.CardColor.LOCOMOTIVE);
            }
        }
        return validColors;
    }

    public void cancelClaimMode() {
        if (claimModeActive) {
            claimModeActive = false;
            mainFrame.getBoardPanel().clearSelection();
            mainFrame.setTitle("London Map - Game On");
        }
    }

    public boolean isClaimModeActive() {
        return claimModeActive;
    }

    public ClaimHistory getClaimHistory() {
        return claimHistory;
    }
}
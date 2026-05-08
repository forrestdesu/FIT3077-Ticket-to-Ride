// src/controller/ClaimRouteCommand.java
package src.controller;

import src.model.Player;
import src.model.Route;
import src.model.TrainCarCard;

import src.exception.RouteAlreadyClaimed;
import src.exception.InsufficientTrainCar;
import src.model.DoubleRoute;

/**
 * design patern: Command Pattern
 *
 * this class represents a single claim a route action as an object
 * instead of having the controller directly contain all the claiming logic,
 * we wrap the entire action into this command object
 *
 * each ClaimRouteCommand stores: who is claiming (the player), what they are claiming (the route)
 * and how they are paying (which card colour + how many locomotives)
 *
 * this lets us validate the action before executing it
 * execute it cleanly in one step
 * and keep history of all claims
 * (requirement: track which player has what routes)
 *
 *
 */
public class ClaimRouteCommand {

    // notes to self: player who wants to claim route
    private final Player player;

    // route on board that player wants to claim
    private final Route route;

    // colour of train car cards the player will spend
    // (and for grey routes player picks which colour to use)
    private final TrainCarCard.CardColor cardColorToUse;



    // Creates a new claim route command

    public ClaimRouteCommand(Player player, Route route, TrainCarCard.CardColor cardColorToUse) {
        this.player = player;
        this.route = route;
        this.cardColorToUse = cardColorToUse;
    }

    // getters so other classes can read command

    // Returns the player who is claiming
    public Player getPlayer() {
        return player;
    }

    // returns the route being claimed
    public Route getRoute() {
        return route;
    }

    // returns the card colour being used to pay
    public TrainCarCard.CardColor getCardColorToUse() {
        return cardColorToUse;
    }
}

    /**
     * validates whether this claim action is legal
     *
     * checks the rules from the rulebook:
     * - route must not already be claimed by someone
     * - in a 2-player game, if one side of a double route is claimed, the other side is blocked
     * -  player must have enough train pieces
     * - player must have enough matching cards
     *
     *  rule is broken - an exception is thrown with a message
     * explaining what went wrong (show to the player as pop up)
     *
     * @throws RouteAlreadyClaimed  if the route or its parallel is taken
     * @throws InsufficientTrainCar if the player lacks pieces or cards
     */
    public void validate() throws RouteAlreadyClaimed, InsufficientTrainCar {

        // check: is the route already claimed?
        // every route can only be claimed once.
        // of someone already owns -no one else can take
        if (route.isRouteClaimed()) {
            throw new RouteAlreadyClaimed(
                    "This route has already been claimed by " + route.getClaimedBy().getPlayerName() + ".");
        }

        // check no2: double route restriction for 2-player games
        // some cities are connected by two parallel routes
        // In a 2-player game, rulebook says: if one side is claimed, other side is blocked — no one can use it.
        // so have to check if this route is a doubel route, and if its parallel partner
        // is already claimed.

        if (route instanceof DoubleRoute) {
            DoubleRoute doubleRoute = (DoubleRoute) route;
            DoubleRoute parallel = doubleRoute.getParallelRoute();

            if (parallel != null && parallel.isRouteClaimed()) {
                throw new RouteAlreadyClaimed(
                        "In a 2-player game, only one side of a double route can be used. "
                                + "The parallel route is already claimed by "
                                + parallel.getClaimedBy().getPlayerName() + ".");
            }
        }

        // check 3: does the player have enough train pieces ?
        // each space on a route needs one physical train piece
        // A route of length 4 needs 4 train pieces
        // if the player has fewer pieces than the route length - they can't claim
        if (player.getTrainPiecesRemaining() < route.getRouteLength()) {
            throw new InsufficientTrainCar( "You only have " + player.getTrainPiecesRemaining()
                            + " train pieces but this route requires "
                            + route.getRouteLength() + ".");
        }

        // check 4: does the player have enough matching cards?
        // to claim a route, need cards matching the route's colour
        // locomotives (wild cards) can substitute for any colour
        int colorCardCount = player.getPlayerhand().cardColorCount(cardColorToUse);
        int locomotiveCount = player.getPlayerhand().locomotiveCount();

        if (cardColorToUse == TrainCarCard.CardColor.LOCOMOTIVE) {
            // if using only locomotives — just check locomotive count
            if (locomotiveCount < route.getRouteLength()) {
                throw new InsufficientTrainCar( "You don't have enough Locomotive cards. You need "
                                + route.getRouteLength() + " but only have "
                                + locomotiveCount + ".");
            }
        } else {
            // using a colour + some locomotives to fill the gap
            if (colorCardCount + locomotiveCount < route.getRouteLength()) {
                throw new InsufficientTrainCar(
                        "You don't have enough cards. You need " + route.getRouteLength() + " "
                                + cardColorToUse.name() + " cards (locomotives can substitute). You have "
                                + colorCardCount + " " + cardColorToUse.name() + " and " + locomotiveCount + " Locomotive(s).");
            }
        }
    }
}
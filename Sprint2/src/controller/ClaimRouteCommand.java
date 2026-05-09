// src/controller/ClaimRouteCommand.java
package src.controller;

import src.exception.InsufficientTrainCar;
import src.exception.RouteAlreadyClaimed;
import src.model.*;

import java.util.ArrayList;
import java.util.List;
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
    /**
     * execute claim action — chaning game state
     *
     * 1. figure out how many colour cards vs locomotives to spend
     * 2. remove those cards from the player's hand
     * 3. put the removed cards into the discard pile
     * 4. mark the route as claimed by this player
     *    (this also deducts train pieces and adds score points)
     *
     * Imp note to self: call validate() before execute()
     * execute() only if all checks have already passed
     *
     * "card deck" - needed to discard used cards
     */
    public void execute(CardDeck cardDeck) {
        int routeLength = route.getRouteLength();
        Hand hand = player.getPlayerhand();

        // 1: calculate how many of each card type to spend
        // use as many colour cards as possible first, then fill the remaining slots with locomotives (wild cards)
        //
        int colorAvailable = hand.cardColorCount(cardColorToUse);
        int colorToSpend = Math.min(colorAvailable, routeLength);
        int locosToSpend = routeLength - colorToSpend;

        // if paying entirely with locomotives, different calculation
        if (cardColorToUse == TrainCarCard.CardColor.LOCOMOTIVE) {
            colorToSpend = routeLength;
            locosToSpend = 0;
        }

        // 2: remove cards from the player's hand
        // removeCardsFromHand returns the actual card objects that were removed
        List<TrainCarCard> removedColorCards =
                hand.removeCardsFromHand(cardColorToUse, colorToSpend);

        List<TrainCarCard> removedLocoCards = new ArrayList<>();
        if (locosToSpend > 0) {
            removedLocoCards =
                    hand.removeCardsFromHand(TrainCarCard.CardColor.LOCOMOTIVE, locosToSpend);
        }

        // 3:: discard the used cards
        // used cards go to the discard pile, not back into the deck
        // when the draw pile runs out, the discard pile gets reshuffled
        for (TrainCarCard card : removedColorCards) {
            cardDeck.discardCard(card);
        }
        for (TrainCarCard card : removedLocoCards) {
            cardDeck.discardCard(card);
        }

        // 4: claim the route
        // Player.claimRoute():
        //   a) marks the route as owned by this player
        //   b) add the route to the player's claimed routes list
        //   c) deduct train pieces from the player's supply
        //   d) adds score points based on route length
        player.claimRoute(route);
    }
}

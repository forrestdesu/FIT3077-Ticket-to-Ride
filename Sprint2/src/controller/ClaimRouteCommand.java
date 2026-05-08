// src/controller/ClaimRouteCommand.java
package src.controller;

import src.model.Player;
import src.model.Route;
import src.model.TrainCarCard;

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
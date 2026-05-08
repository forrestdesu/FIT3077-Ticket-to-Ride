package src.controller;

import src.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * design pattern: Command Pattern — history component
 *
 * stores the history of all successfully executed ClaimRouteCommands
 *
 * after a ClaimRouteCommand is validated and executed, i want to store it here
 * giving me a complete record of
 * which player claimed which route
 * what cards they used to pay for it
 * the order in which routes were claimed
 *
 */
public class ClaimHistory {

    // The list of all executed claim commands, in chronological order
    private final List<ClaimRouteCommand> executedCommands;

    /** create an empty claim history. */
    public ClaimHistory() {
        this.executedCommands = new ArrayList<>();
    }

    /**
     * record executed command in the history
     * call this after command.execute() succeeds
     *
     */
    public void recordClaim(ClaimRouteCommand command) {
        executedCommands.add(command); // "command" - command that was just executed
    }

    /**
     * returns all claim commands executed so far
     * the returned list cannot be modified
     */
    public List<ClaimRouteCommand> getAllClaims() {
        return Collections.unmodifiableList(executedCommands); // returns unmodifiable list of all executed commands
    }

    /**
     * return only the claims made by the specific player
     * for displaying a player's claim history.
     *

     * player: the player to filter by
     * return: list of that player's claim commands
     */
    public List<ClaimRouteCommand> getClaimsForPlayer(Player player) {
        List<ClaimRouteCommand> playerClaims = new ArrayList<>();
        for (ClaimRouteCommand command : executedCommands) {
            if (command.getPlayer().equals(player)) { //
                playerClaims.add(command);
            }
        }
        return playerClaims;
    }

    /** returns total number of routes claimed so far across all players. */
    public int getTotalClaimCount() {
        return executedCommands.size();
    }
}
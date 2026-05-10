package src.util;

import src.model.DestinationTicket;
import src.model.Player;
import src.model.Route;

import java.util.List;

public class ScoreCalculator {

    /** Points awarded to the player(s) with the longest continuous path. */
    public static final int LONGEST_PATH_BONUS = 10;

    public static int[] applyFinalScores(List<Player> players) {
        applyIncompleteTicketDeductions(players);
        return applyLongestPathBonus(players);
    }

    /**
     * Deducts points for any destination ticket that has not been completed.
     * Re-checks completion one final time in case the last claimed route finished it.
     */
    private static void applyIncompleteTicketDeductions(List<Player> players) {
        for (Player player : players) {
            for (DestinationTicket ticket : player.getPlayerTickets()) {
                if (!ticket.isCompleted()) {
                    if (TicketCompletionChecker.isTicketCompleted(player, ticket)) {
                        ticket.setCompleted(true);
                        player.adjustPlayerScore(ticket.getPoints());
                    } else {
                        player.adjustPlayerScore(-ticket.getPoints());
                    }
                }
            }
        }
    }

    /**
     * Awards the 10-point longest-path bonus to the player(s) with the longest
     * continuous path. In case of a tie, all tied players receive the bonus.
     */
    private static int[] applyLongestPathBonus(List<Player> players) {
        int[] pathLengths = new int[players.size()];
        int maxPath = 0;

        for (int i = 0; i < players.size(); i++) {
            pathLengths[i] = LongestPathCalculator.calculate(players.get(i));
            if (pathLengths[i] > maxPath) {
                maxPath = pathLengths[i];
            }
        }

        if (maxPath > 0) {
            for (int i = 0; i < players.size(); i++) {
                if (pathLengths[i] == maxPath) {
                    players.get(i).adjustPlayerScore(LONGEST_PATH_BONUS);
                }
            }
        }

        return pathLengths;
    }

    /** Convenience: points scored for claiming a route of the given length. */
    public static int routePoints(int length) {
        return Route.pointsForRouteLength(length);
    }
}
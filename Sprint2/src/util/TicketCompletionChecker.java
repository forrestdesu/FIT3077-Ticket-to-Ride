package src.util;

import src.model.DestinationTicket;
import src.model.Player;
import src.model.Route;

import java.util.*;

public class TicketCompletionChecker {

    public static boolean isTicketCompleted(Player player, DestinationTicket ticket) {
        String startCity = ticket.getCity1();
        String endCity = ticket.getCity2();

        Map<String, Set<String>> adjacency = new HashMap<>();
        for (Route route : player.getClaimedRoutes()) {
            String c1 = route.getCity1().getName();
            String c2 = route.getCity2().getName();
            adjacency.computeIfAbsent(c1, k -> new HashSet<>()).add(c2);
            adjacency.computeIfAbsent(c2, k -> new HashSet<>()).add(c1);
        }

        if (!adjacency.containsKey(startCity)) {
            return false;
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(startCity);
        visited.add(startCity);

        while (!queue.isEmpty()) {
            String currentCity = queue.poll();
            if (currentCity.equals(endCity)) {
                return true;
            }
            Set<String> neighbours = adjacency.getOrDefault(currentCity, Collections.emptySet());
            for (String neighbour : neighbours) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }
        return false;
    }
}
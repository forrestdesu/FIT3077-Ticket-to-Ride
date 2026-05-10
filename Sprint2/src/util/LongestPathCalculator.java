package src.util;

import src.model.Player;
import src.model.Route;

import java.util.*;

public class LongestPathCalculator {

    public static int calculate(Player player) {
        List<Route> claimed = player.getClaimedRoutes();
        if (claimed.isEmpty()) {
            return 0;
        }

        // Build adjacency: cityName -> list of (neighbourCityName, routeIndex)
        // We track route index so we can mark individual edge visits.
        Map<String, List<int[]>> adjacency = new HashMap<>();
        for (int i = 0; i < claimed.size(); i++) {
            Route route = claimed.get(i);
            String c1 = route.getCity1().getName();
            String c2 = route.getCity2().getName();
            adjacency.computeIfAbsent(c1, k -> new ArrayList<>()).add(new int[]{i});
            adjacency.computeIfAbsent(c2, k -> new ArrayList<>()).add(new int[]{i});
        }

        // Try starting DFS from every city that has at least one claimed route endpoint
        int[] best = {0};
        boolean[] usedEdge = new boolean[claimed.size()];

        for (String startCity : adjacency.keySet()) {
            dfs(startCity, adjacency, claimed, usedEdge, 0, best);
        }

        return best[0];
    }

    private static void dfs(
            String city,
            Map<String, List<int[]>> adjacency,
            List<Route> routes,
            boolean[] usedEdge,
            int current,
            int[] best) {

        if (current > best[0]) {
            best[0] = current;
        }

        List<int[]> edges = adjacency.getOrDefault(city, Collections.emptyList());
        for (int[] edgeRef : edges) {
            int routeIdx = edgeRef[0];
            if (usedEdge[routeIdx]) {
                continue;
            }

            Route route = routes.get(routeIdx);
            String neighbour = route.getCity1().getName().equals(city)
                    ? route.getCity2().getName()
                    : route.getCity1().getName();

            usedEdge[routeIdx] = true;
            dfs(neighbour, adjacency, routes, usedEdge, current + route.getRouteLength(), best);
            usedEdge[routeIdx] = false; // backtrack
        }
    }
}
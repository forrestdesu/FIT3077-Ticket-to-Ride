package src.util;

import src.model.Player;
import src.model.Route;

public class ScoreCalculator {
    public void calculateFinalScore(Player player){
        //probably make a map navigation algorithm like BFS or DFS
        // player.adjustPlayerScore(); - use this for score adjustment
    }
    public static int routePoints(int length){
        return Route.pointsForRouteLength(length);
    }
}

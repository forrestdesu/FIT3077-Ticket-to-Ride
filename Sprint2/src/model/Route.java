package src.model;

import java.awt.*;

public class Route {
    public enum RouteColor {
        BLUE, GREEN, YELLOW, BLACK, ORANGE, PINK, GREY
    }

    private final String id;
    private final City city1;
    private final City city2;
    private final int routeLength;
    private final RouteColor routeColor;
    private Player claimedBy;

    public Route(String id, City city1, City city2, int routeLength, RouteColor routeColor) {
        this.id = id;
        this.city1 = city1;
        this.city2 = city2;
        this.routeLength = routeLength;
        this.routeColor = routeColor;
//        if(this.routeLength == 5) {
//            this.claimedBy = new Player("Player New", Color.BLACK);
            this.claimedBy = null;
//        }
    }

    public String getId(){
        return id;
    }

    public City getCity1(){
        return city1;
    }

    public City getCity2(){
        return city2;
    }

    public int getRouteLength(){
        return routeLength;
    }

    public RouteColor getRouteColor(){
        return routeColor;
    }

    public boolean isRouteClaimed(){
        return claimedBy != null;
    }

    public Player getClaimedBy() {
        return claimedBy;
    }

    public void claimRoute(Player player){
        this.claimedBy = player;
    }

    public boolean connectsTo(String cityName){
        return city1.getName().equals(cityName) || city2.getName().equals(cityName);
    }

    public String getOtherCity(String cityName){
        return city1.getName().equals(cityName) ? city2.getName() : city2.getName().equals(cityName) ? city1.getName() : null;
    }

    //the relative point to get after claiming a route
    public static int pointsForRouteLength(int routeLength){
        return switch (routeLength) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 7;
            case 5 -> 10;
            case 6 -> 15;
            default -> 0;
        };
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

package src.model;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class Player {
    //fixed values
    public static final int STARTING_TRAIN_PIECES = 17;
    public static final int INITIAL_HAND_SIZE_FOR_TRAIN_CARDS = 45;

    //all elements the player is connected to
    private final String playerName;
    private final Color playerColor;
    private final Hand playerhand;
    private final List<DestinationTicket> playerTickets;
    private final List<Route> claimedRoutes;
    private int trainPiecesRemaining;
    private int playerScore;

    public Player(String playerName, Color playerColor){
        this.playerName = playerName;
        this.playerColor = playerColor;
        this.playerhand = new Hand();
        this.playerTickets = new ArrayList<>();
        this.claimedRoutes = new ArrayList<>();
        this.trainPiecesRemaining = STARTING_TRAIN_PIECES;
        this.playerScore = 0;
    }

    public String getPlayerName(){
        return playerName;
    }

    public Color getPlayerColor(){
        return playerColor;
    }

    public Hand getPlayerhand(){
        return playerhand;
    }

    public List<DestinationTicket> getPlayerTickets(){
        return Collections.unmodifiableList(playerTickets);
    }

    public void addTickets(DestinationTicket ticket){
        playerTickets.add(ticket);
    }

    public void removeTicket(DestinationTicket ticket){
        playerTickets.remove(ticket);
    }

    public List<Route> getClaimedRoutes(){
        return Collections.unmodifiableList(claimedRoutes);
    }

    public void claimRoute(Route route){
        route.claimRoute(this);
        claimedRoutes.add(route);
        trainPiecesRemaining -= route.getRouteLength();
        playerScore += Route.pointsForRouteLength(route.getRouteLength());
    }

    public int getTrainPiecesRemaining(){
        return trainPiecesRemaining;
    }

    public int getPlayerScore(){
        return playerScore;
    }

    public void adjustPlayerScore(int score){
        playerScore += score;
    }
}

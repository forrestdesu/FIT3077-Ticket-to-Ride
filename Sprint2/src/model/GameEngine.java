package src.model;

import src.interfaces.GameObserver;
import src.util.MapDataHandler;
import src.view.BoardPanel;

import javax.smartcardio.Card;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    public static final int FACE_UP_CARD_COUNT = 5;
    public static final int INITIAL_TRAIN_CARDS_IN_HAND = 4;
    public static final int MIN_INITIAL_TICKETS_TO_KEEP = 2;
    public static final int MIN_MIDGAME_TICKETS_TO_KEEP = 1;

    private static final Color[] PLAYER_COLORS = {new Color(220, 53, 64), new Color(0, 123, 255), new Color(40, 167, 69), new Color(255, 193, 7)};

    private final BoardMap boardMap;
    private final CardDeck cardDeck;
    private final TicketDeck ticketDeck;
    private final List<TrainCarCard> faceUpCards;
    private final List<Player> players;
    private final List<GameObserver> observers;

    private int currentPlayerIndex;
    private boolean gameOver;
    private boolean lastGameRoundTriggered;
    private int lastGameRoundStartPlayerIndex;

    public GameEngine(List<String> playerName){
        if(playerName == null || playerName.size() < 2){
            throw new IllegalArgumentException("Ticket to Ride London requires at least 2 player");
        }

        observers = new ArrayList<>();

        MapDataHandler handler = new MapDataHandler();
        boardMap = handler.loadBoardMap();
        ticketDeck = handler.loadTicketDeck();
        cardDeck = new CardDeck();
        faceUpCards = new ArrayList<>();

        players = new ArrayList<>();
        for (int i = 0; i < playerName.size(); i++){
            Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];
            players.add(new Player(playerName.get(i), color));
        }

        setupGame();
    }

    private void setupGame(){
        setupInitialPlayerHand();
        refillFaceUpCards();
        dealInitialTickets();
        currentPlayerIndex = 0;
        gameOver = false;
        lastGameRoundTriggered = false;
        lastGameRoundStartPlayerIndex = -1;
    }

    private void setupInitialPlayerHand(){
        for (Player player : players){
            player.getPlayerhand().addCards(cardDeck.drawMultipleCards(INITIAL_TRAIN_CARDS_IN_HAND));
        }
    }

    public void refillFaceUpCards(){
        while(faceUpCards.size() < FACE_UP_CARD_COUNT && !cardDeck.isCardDeckEmpty()){
            faceUpCards.add(cardDeck.drawCard());
        }
        enforceLocomotiveRule();
    }

    private void enforceLocomotiveRule(){
        for(int attempt = 0; attempt < 10; attempt++){
            int locomotiveCount = 0;
            for(TrainCarCard carCard : faceUpCards){
                if(carCard.isLocomotive()){
                    locomotiveCount++;
                }
            }
            if(locomotiveCount < 3){
                break;
            }
            for(TrainCarCard carCard : faceUpCards){
                cardDeck.discardCard(carCard);
            }
            faceUpCards.clear();
            while(faceUpCards.size() < FACE_UP_CARD_COUNT && !cardDeck.isCardDeckEmpty()){
                faceUpCards.add(cardDeck.drawCard());
            }
        }
    }

    private void dealInitialTickets(){
        for (Player player : players){
            List<DestinationTicket> dealtTickets = ticketDeck.drawThreeTickets();
            for(DestinationTicket ticket : dealtTickets){
                player.addTickets(ticket);
            }
        }
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(){
        for(GameObserver observer : observers){
            observer.onGameStateChange();
        }
    }

    public void nextPlayerTurn(){
        Player current = getCurrentPlayer();

        if(!lastGameRoundTriggered && current.getTrainPiecesRemaining() <= 2){
            lastGameRoundTriggered = true;
            lastGameRoundStartPlayerIndex = currentPlayerIndex;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        if(lastGameRoundTriggered && currentPlayerIndex == lastGameRoundStartPlayerIndex){
            gameOver = true;
        }

        notifyObservers();
    }

    public BoardMap getBoardMap(){
        return boardMap;
    }

    public CardDeck getCardDeck(){
        return cardDeck;
    }

    public TicketDeck getTicketDeck(){
        return ticketDeck;
    }

    public List<TrainCarCard> getFaceUpCards(){
        return faceUpCards;
    }

    public List<Player> getPlayers(){
        return players;
    }

    public Player getPlayer1(){
        return players.get(0);
    }

    public Player getPlayer2(){
        return players.get(1);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Player getOtherPlayer(){
        return players.get((currentPlayerIndex + 1) % players.size());
    }

    public boolean isGameOver(){
        return gameOver;
    }

    public boolean isLastGameRoundTriggered(){
        return lastGameRoundTriggered;
    }
}

package src.model;

import src.interfaces.GameObserver;

import java.awt.*;
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

    }
}

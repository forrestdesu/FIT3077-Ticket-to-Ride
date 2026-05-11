package src.model;

import src.interfaces.GameObserver;
import src.util.LongestPathCalculator;
import src.util.MapDataHandler;
import src.util.ScoreCalculator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    //fixed values
    public static final int FACE_UP_CARD_COUNT = 5;
    public static final int INITIAL_TRAIN_CARDS_IN_HAND = 4;
    public static final int MIN_INITIAL_TICKETS_TO_KEEP = 2;
    public static final int MIN_MIDGAME_TICKETS_TO_KEEP = 1;

    private static final Color[] PLAYER_COLORS = {
            new Color(220, 53, 64),
            new Color(0, 123, 255),
            new Color(40, 167, 69),
            new Color(255, 193, 7)
    };

    //all the related elements that the game engine controls and gets feedback from
    private final BoardMap boardMap;
    private final CardDeck cardDeck;
    private final TicketDeck ticketDeck;
    private final List<TrainCarCard> faceUpCards;
    private final List<Player> players;
    private final List<GameObserver> observers;

    private int currentPlayerIndex;
    private boolean gameOver;
    private boolean finalScoresApplied;
    private boolean lastGameRoundTriggered;
    private int triggeringPlayerIndex;
    private int lastFinalTurnPlayerIndex;

    // Stored after applyFinalScores() so the summary can reference them
    private int[] longestPathLengths;

    public GameEngine(List<String> playerName) {
        if (playerName == null || playerName.size() < 2) {
            throw new IllegalArgumentException("Ticket to Ride London requires at least 2 players");
        }

        observers = new ArrayList<>();

        MapDataHandler handler = new MapDataHandler();
        boardMap = handler.loadBoardMap();
        ticketDeck = handler.loadTicketDeck();
        cardDeck = new CardDeck();
        faceUpCards = new ArrayList<>();

        players = new ArrayList<>();
        for (int i = 0; i < playerName.size(); i++) {
            Color color = PLAYER_COLORS[i % PLAYER_COLORS.length];
            players.add(new Player(playerName.get(i), color));
        }

        setupGame();
    }

    private void setupGame() {
        setupInitialPlayerHand();
        refillFaceUpCards();
        dealInitialTickets();
        currentPlayerIndex = 0;
        gameOver = false;
        finalScoresApplied = false;
        lastGameRoundTriggered = false;
        triggeringPlayerIndex = -1;
        lastFinalTurnPlayerIndex = -1;
        longestPathLengths = null;
    }

    private void setupInitialPlayerHand() {
        for (Player player : players) {
            player.getPlayerhand().addCards(cardDeck.drawMultipleCards(INITIAL_TRAIN_CARDS_IN_HAND));
        }
    }

    public void refillFaceUpCards() {
        while (faceUpCards.size() < FACE_UP_CARD_COUNT && !cardDeck.isCardDeckEmpty()) {
            faceUpCards.add(cardDeck.drawCard());
        }
        enforceLocomotiveRule();
    }

    //this is an automatic reset of the face up cards, this allows the reshuffling of up to 10 times, so that we dont have more than 3 locomotives
    private void enforceLocomotiveRule() {
        for (int attempt = 0; attempt < 10; attempt++) {
            int locomotiveCount = 0;
            for (TrainCarCard carCard : faceUpCards) {
                if (carCard.isLocomotive()) locomotiveCount++;
            }
            if (locomotiveCount < 3) break;
            for (TrainCarCard carCard : faceUpCards) {
                cardDeck.discardCard(carCard);
            }
            faceUpCards.clear();
            while (faceUpCards.size() < FACE_UP_CARD_COUNT && !cardDeck.isCardDeckEmpty()) {
                faceUpCards.add(cardDeck.drawCard());
            }
        }
    }

    private void dealInitialTickets() {
        for (Player player : players) {
            List<DestinationTicket> dealtTickets = ticketDeck.drawThreeTickets();
            for (DestinationTicket ticket : dealtTickets) {
                player.addTickets(ticket);
            }
        }
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    //observers to notify change that update the whole UI
    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged();
        }
    }

    public void nextPlayerTurn() {
        Player current = getCurrentPlayer();

        // Trigger the final round the first time any player ends their turn at <= 2 trains.
        if (!lastGameRoundTriggered && current.getTrainPiecesRemaining() <= 2) {
            lastGameRoundTriggered = true;
            triggeringPlayerIndex = currentPlayerIndex;
            // lastFinalTurnPlayerIndex is the triggering player — the game ends
            // after they complete their OWN additional final turn (after all other
            // players have also had theirs). We use a separate flag to know we
            // have already passed the triggering turn once.
            lastFinalTurnPlayerIndex = currentPlayerIndex;
        } else if (lastGameRoundTriggered && currentPlayerIndex == lastFinalTurnPlayerIndex) {
            // The triggering player has now completed their additional final turn —
            // all other players already went, so the game is over.
            gameOver = true;
        }

        // Advance to the next player
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        notifyObservers();
    }

    public boolean isCurrentPlayerOnFinalTurn() {
        // No final round in progress, or the game is already over
        if (!lastGameRoundTriggered || gameOver) {
            return false;
        }
        // All players in the final round get a warning
        return true;
    }

    /**
     * Applies final scoring exactly once, then builds and returns the full
     * score summary string (with longest-path breakdown) for display.
     */
    public String applyAndBuildFinalScoreSummary() {
        if (!finalScoresApplied) {
            longestPathLengths = ScoreCalculator.applyFinalScores(players);
            finalScoresApplied = true;
        }

        int maxPath = 0;
        for (int len : longestPathLengths) {
            if (len > maxPath) maxPath = len;
        }

        // Determine winner: most points, then most completed tickets (rulebook tie-break)
        Player winner = players.get(0);
        for (Player p : players) {
            if (p.getPlayerScore() > winner.getPlayerScore()) {
                winner = p;
            } else if (p.getPlayerScore() == winner.getPlayerScore()
                    && completedTicketCount(p) > completedTicketCount(winner)) {
                winner = p;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════\n");
        sb.append("    GAME OVER — FINAL SCORES\n");
        sb.append("═══════════════════════════════\n\n");

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int pathLen = longestPathLengths[i];
            boolean hasLongest = pathLen == maxPath && maxPath > 0;

            sb.append("▶  ").append(p.getPlayerName()).append("\n");
            sb.append("   Route points:         ").append(routePointsFor(p)).append("\n");
            appendTicketBreakdown(sb, p);

            sb.append("   Longest path:         ")
                    .append(pathLen).append(" segments");
            if (hasLongest) {
                sb.append("  →  +").append(ScoreCalculator.LONGEST_PATH_BONUS)
                        .append(" pts bonus 🏅");
            }
            sb.append("\n");

            sb.append("   Trains remaining:     ").append(p.getTrainPiecesRemaining()).append("\n");
            sb.append("   ──────────────────────────\n");
            sb.append("   TOTAL SCORE:          ").append(p.getPlayerScore()).append(" pts\n");
            sb.append("\n");
        }

        sb.append("═══════════════════════════════\n");
        sb.append("🏆  WINNER:  ").append(winner.getPlayerName())
                .append("  (").append(winner.getPlayerScore()).append(" pts)\n");
        sb.append("═══════════════════════════════");

        return sb.toString();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private int completedTicketCount(Player player) {
        int count = 0;
        for (DestinationTicket t : player.getPlayerTickets()) {
            if (t.isCompleted()) count++;
        }
        return count;
    }

    private int routePointsFor(Player player) {
        int total = 0;
        for (Route route : player.getClaimedRoutes()) {
            total += Route.pointsForRouteLength(route.getRouteLength());
        }
        return total;
    }

    private void appendTicketBreakdown(StringBuilder sb, Player player) {
        for (DestinationTicket ticket : player.getPlayerTickets()) {
            String status = ticket.isCompleted() ? "✓ +" : "✗  -";
            sb.append("   Ticket ").append(status).append(ticket.getPoints())
                    .append(" pts  (").append(ticket.getCity1())
                    .append(" ↔ ").append(ticket.getCity2()).append(")\n");
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public BoardMap getBoardMap() { return boardMap; }
    public CardDeck getCardDeck() { return cardDeck; }
    public TicketDeck getTicketDeck() { return ticketDeck; }
    public List<TrainCarCard> getFaceUpCards() { return faceUpCards; }
    public List<Player> getPlayers() { return players; }
    public Player getPlayer1() { return players.get(0); }
    public Player getPlayer2() { return players.get(1); }
    public Player getCurrentPlayer() { return players.get(currentPlayerIndex); }
    public Player getOtherPlayer() { return players.get((currentPlayerIndex + 1) % players.size()); }
    public boolean isGameOver() { return gameOver; }
    public boolean isLastGameRoundTriggered() { return lastGameRoundTriggered; }
}
package src.model;

import src.interfaces.GameObserver;
import src.util.MapDataHandler;
import src.util.ScoreCalculator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameEngine {
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
    private int lastGameRoundStartPlayerIndex;

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
        lastGameRoundStartPlayerIndex = -1;
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

    private void enforceLocomotiveRule() {
        for (int attempt = 0; attempt < 10; attempt++) {
            int locomotiveCount = 0;
            for (TrainCarCard carCard : faceUpCards) {
                if (carCard.isLocomotive()) {
                    locomotiveCount++;
                }
            }
            if (locomotiveCount < 3) {
                break;
            }
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

    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged();
        }
    }

    /**
     * Advances the turn to the next player and checks end-game conditions.
     *
     * <p>Per the rulebook: when a player ends their turn with 0, 1, or 2 train pieces
     * remaining, the final round is triggered. Every player (including that player)
     * gets exactly one more turn, then the game ends.
     */
    public void nextPlayerTurn() {
        Player current = getCurrentPlayer();

        // Trigger the final round the first time any player drops to <= 2 trains
        if (!lastGameRoundTriggered && current.getTrainPiecesRemaining() <= 2) {
            lastGameRoundTriggered = true;
            lastGameRoundStartPlayerIndex = currentPlayerIndex;
        }

        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        // The game ends once the turn wraps back to the player who triggered the last round
        if (lastGameRoundTriggered && currentPlayerIndex == lastGameRoundStartPlayerIndex) {
            gameOver = true;
        }

        notifyObservers();
    }

    /**
     * Returns {@code true} if the current player is taking their final turn.
     * Used by TurnController to show the "final turn" warning popup before
     * the player performs any action.
     */
    public boolean isCurrentPlayerOnFinalTurn() {
        return lastGameRoundTriggered && !gameOver;
    }

    /**
     * Applies final scoring (incomplete ticket deductions + longest-path bonus)
     * exactly once, then returns the score summary as a formatted string for display.
     *
     * @return multi-line summary string suitable for a JOptionPane message
     */
    public String applyAndBuildFinalScoreSummary() {
        if (!finalScoresApplied) {
            ScoreCalculator.applyFinalScores(players);
            finalScoresApplied = true;
        }

        // Find winner(s)
        int maxScore = Integer.MIN_VALUE;
        int maxTickets = -1;
        for (Player p : players) {
            if (p.getPlayerScore() > maxScore) {
                maxScore = p.getPlayerScore();
                maxTickets = completedTicketCount(p);
            } else if (p.getPlayerScore() == maxScore) {
                int tc = completedTicketCount(p);
                if (tc > maxTickets) {
                    maxTickets = tc;
                }
            }
        }

        // Determine winner(s) applying tie-break rules from rulebook:
        // 1. Most points; 2. Most completed tickets; 3. Longest path (already in score)
        Player winner = players.get(0);
        for (Player p : players) {
            if (p.getPlayerScore() > winner.getPlayerScore()) {
                winner = p;
            } else if (p.getPlayerScore() == winner.getPlayerScore()) {
                if (completedTicketCount(p) > completedTicketCount(winner)) {
                    winner = p;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════\n");
        sb.append("        GAME OVER — FINAL SCORES\n");
        sb.append("═══════════════════════════════\n\n");

        for (Player p : players) {
            sb.append("▶  ").append(p.getPlayerName()).append("\n");
            sb.append("   Route points:      ").append(routePointsFor(p)).append("\n");
            appendTicketBreakdown(sb, p);
            sb.append("   Trains remaining:  ").append(p.getTrainPiecesRemaining()).append("\n");
            sb.append("   TOTAL SCORE:       ").append(p.getPlayerScore()).append(" pts\n");
            sb.append("\n");
        }

        sb.append("═══════════════════════════════\n");
        sb.append("🏆  WINNER:  ").append(winner.getPlayerName())
                .append("  (").append(winner.getPlayerScore()).append(" pts)\n");
        sb.append("═══════════════════════════════");

        return sb.toString();
    }

    /** Counts how many destination tickets the player has completed. */
    private int completedTicketCount(Player player) {
        int count = 0;
        for (src.model.DestinationTicket t : player.getPlayerTickets()) {
            if (t.isCompleted()) count++;
        }
        return count;
    }

    /**
     * Calculates the route-only points for a player by summing points for each
     * claimed route. Used only for the final score breakdown display.
     */
    private int routePointsFor(Player player) {
        int total = 0;
        for (Route route : player.getClaimedRoutes()) {
            total += Route.pointsForRouteLength(route.getRouteLength());
        }
        return total;
    }

    /** Appends per-ticket breakdown lines to the summary string builder. */
    private void appendTicketBreakdown(StringBuilder sb, Player player) {
        for (src.model.DestinationTicket ticket : player.getPlayerTickets()) {
            String status = ticket.isCompleted() ? "✓ +" : "✗ -";
            sb.append("   Ticket ").append(status).append(ticket.getPoints())
                    .append(" pts  (").append(ticket.getCity1())
                    .append(" ↔ ").append(ticket.getCity2()).append(")\n");
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

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
package src.model;

import java.util.*;
import java.util.concurrent.TransferQueue;

public class Hand {
    private final List<TrainCarCard> playerCards;

    public Hand(){
        playerCards = new ArrayList<>();
    }

    public void addCard(TrainCarCard newCard){
        playerCards.add(newCard);
    }

    public void addCards(List<TrainCarCard> newCards){
        playerCards.addAll(newCards);
    }

    public int cardColorCount(TrainCarCard.CardColor cardColor){
        int count = 0;
        for (TrainCarCard card : playerCards){
            if(card.getCardColor() == cardColor) {
                count++;
            }
        }
        return count;
    }

    public int locomotiveCount(){
        return cardColorCount(TrainCarCard.CardColor.LOCOMOTIVE);
    }

    public int handSize(){
        return playerCards.size();
    }

    public List<TrainCarCard> removeCardsFromHand(TrainCarCard.CardColor cardColor, int count){
        List<TrainCarCard> removed = new ArrayList<>();
        int remainingToRemove = count;

        for (int i = playerCards.size() - 1; i >= 0 && remainingToRemove > 0; i--){
            if(playerCards.get(i).getCardColor() == cardColor){
                removed.add(playerCards.remove(i));
                remainingToRemove--;
            }
        }
        return removed;
    }

    public Map<TrainCarCard.CardColor, Integer> getCardColorCounts(){
        Map<TrainCarCard.CardColor, Integer> cardCount = new HashMap<>();
        for(TrainCarCard.CardColor cardColor : TrainCarCard.CardColor.values()){
            int count = cardColorCount(cardColor);
            if (count > 0){
                cardCount.put(cardColor, count);
            }
        }
        return cardCount;
    }

    public List<TrainCarCard> getPlayerCards(){
        return Collections.unmodifiableList(playerCards);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

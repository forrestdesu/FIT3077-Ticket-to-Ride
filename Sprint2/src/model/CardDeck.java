package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck {
    private static final int CARDS_PER_COLOR = 12;
    private static final int LOCOMOTIVE_CARDS = 14;

    private final List<TrainCarCard> drawCardPile;
    private final List<TrainCarCard> discardCardPile;

    public CardDeck(){
        drawCardPile = new ArrayList<>();
        discardCardPile = new ArrayList<>();
        buildCardDeck();
        Collections.shuffle(drawCardPile);
    }

    public void buildCardDeck(){
        TrainCarCard.CardColor [] colors = {TrainCarCard.CardColor.RED, TrainCarCard.CardColor.BLUE, TrainCarCard.CardColor.GREEN, TrainCarCard.CardColor.YELLOW, TrainCarCard.CardColor.BLACK, TrainCarCard.CardColor.WHITE, TrainCarCard.CardColor.ORANGE, TrainCarCard.CardColor.PINK};

        for (TrainCarCard.CardColor color : colors){
            for (int i = 0; i < CARDS_PER_COLOR; i++){
                drawCardPile.add(new TrainCarCard(color));
            }
        }
        for (int i = 0; i < LOCOMOTIVE_CARDS; i++){
            drawCardPile.add(new TrainCarCard(TrainCarCard.CardColor.LOCOMOTIVE));
        }
    }

    public TrainCarCard drawCard(){
        if(drawCardPile.isEmpty()){
            if(discardCardPile.isEmpty()){
                return null;
            }
            drawCardPile.addAll(discardCardPile);
            discardCardPile.clear();
            Collections.shuffle(drawCardPile);
        }
        return drawCardPile.remove(drawCardPile.size() - 1);
    }

    public List<TrainCarCard> drawMultipleCards(int count){
        List<TrainCarCard> drawnCards = new ArrayList<>();

        for (int i = 0; i < count; i++){
            TrainCarCard carCard = drawCard();
            if(carCard != null) {
                drawnCards.add(carCard);
            }
        }
        return drawnCards;
    }

    public void discardCard(TrainCarCard carCard){
        discardCardPile.add(carCard);
    }

    public int cardDeckSize(){
        return drawCardPile.size();
    }

    public boolean isCardDeckEmpty(){
        return drawCardPile.isEmpty();
    }

    public List<TrainCarCard> getDrawCardPile(){
        return Collections.unmodifiableList(drawCardPile);
    }
}

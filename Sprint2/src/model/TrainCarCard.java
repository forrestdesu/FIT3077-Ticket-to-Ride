package src.model;

public class TrainCarCard {
    public enum CardColor{
        RED, BLUE, GREEN, YELLOW, BLACK, WHITE, ORANGE, PINK, LOCOMOTIVE
    }

    private final CardColor cardColor;

    public TrainCarCard(CardColor cardColor){
        this.cardColor = cardColor;
    }

    public CardColor getCardColor(){
        return cardColor;
    }

    public boolean isLocomotive(){
        return cardColor == CardColor.LOCOMOTIVE;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

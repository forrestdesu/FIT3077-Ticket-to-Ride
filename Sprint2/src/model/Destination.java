package src.model;
import java.util.List;
import java.util.Arrays;

public class Destination {
    public static void main(String[] args) {
        List<String> locations = Arrays.asList(
                "King's Cross",
                "St Paul's",
                "Tower of London",
                "Covent Garden",
                "Trafalgar Square",
                "Waterloo",
                "Elephant & Castle",
                "Big Ben",
                "Buckingham Palace",
                "Hyde Park",
                "Piccadilly Circus",
                "British Museum",
                "Baker Street",
                "Regent's Park",
                "The Charterhouse",
                "Brick Lane",
                "Globe Theatre"
        );
        locations.forEach(System.out::println);
    }
}
package src.model;

public class DestinationTicket {
    public final String city1;
    public final String city2;
    public final int points;

    public DestinationTicket(String city1, String city2, int points) {
        this.city1 = city1;
        this.city2 = city2;
        this.points = points;
    }

    public String getCity1() {
        return city1;
    }

    public String getCity2() {
        return city2;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

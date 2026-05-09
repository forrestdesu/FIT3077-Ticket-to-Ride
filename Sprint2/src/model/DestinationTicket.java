package src.model;

public class DestinationTicket {
    public final String city1;
    public final String city2;
    public final int points;
    private boolean completed;

    public DestinationTicket(String city1, String city2, int points) {
        this.city1 = city1;
        this.city2 = city2;
        this.points = points;
        this.completed = false;
    }

    public String getCity1() { return city1; }
    public String getCity2() { return city2; }
    public int getPoints() { return points; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() {
        return city1 + " ↔ " + city2 + " [" + points + " pts]" + (completed ? " ✓" : "");
    }
}
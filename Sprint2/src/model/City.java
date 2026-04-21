package src.model;

public class City {
    private final String id;
    private final String name;
    private final double xPercent;
    private final double yPercent;

    public City(String id, String name, double xPercent, double yPercent){
        this.id = id;
        this.name = name;
        this.xPercent = xPercent;
        this.yPercent = yPercent;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public double getxPercent(){
        return xPercent;
    }

    public double getyPercent(){
        return yPercent;
    }

    public int getPixelX(int panelWidth){
        return (int) (xPercent / 100.0 * panelWidth);
    }

    public int getPixelY(int panelHeight){
        return (int) (xPercent / 100.0 * panelHeight);
    }
}

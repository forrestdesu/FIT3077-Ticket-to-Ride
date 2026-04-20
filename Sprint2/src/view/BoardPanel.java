package src.view;

import src.interfaces.GameObserver;
import src.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel implements GameObserver {
    private static final int CITY_RADIUS = 10;
    private static final int CLICK_THRESHOLD = 18;
    private static final int DOUBLE_ROUTE_OFFSET = 5;

    private final GameEngine gameEngine;
    private Route selectedRoute;

    public BoardPanel(GameEngine gameEngine){
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);
        setBorder(BorderFactory.createTitledBorder("Board"));
        setBackground(Color.WHITE);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void handleClick(int mouseX, int mouseY){
        Route closest = null;
        double bestDist = CLICK_THRESHOLD;

        for (Route route : gameEngine.getBoardMap().getAllRoutes()){
            double dist = distToMidpoint(route, mouseX, mouseY);
            if(dist < bestDist){
                bestDist = dist;
                closest = route;
            }
        }

        selectedRoute = closest;
        repaint();
    }

    private double distToMidpoint(Route route, int mouseX, int mouseY){
        int x1 = route.getCity1().getPixelX(getWidth());
        int y1 = route.getCity1().getPixelY(getHeight());
        int x2 = route.getCity2().getPixelX(getWidth());
        int y2 = route.getCity2().getPixelY(getHeight());
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;
        return Math.hypot(mouseX - midX, mouseY - midY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        BoardMap boardMap = gameEngine.getBoardMap();
        drawRoutes(g2, boardMap);
        drawCities(g2, boardMap);
    }

    private void drawRoutes(Graphics2D g2, BoardMap boardMap){
        for(Route route : boardMap.getAllRoutes()){
            drawRoute(g2, route);
        }
    }

    private void drawRoute(Graphics2D g2, Route route){
        int w = getWidth();
        int h = getHeight();

        double x1 = route.getCity1().getPixelX(w);
        double y1 = route.getCity1().getPixelY(h);
        double x2 = route.getCity2().getPixelX(w);
        double y2 = route.getCity2().getPixelY(h);

        if (route instanceof DoubleRoute) {
            double dx = x2 - x1;
            double dy = y2 - y1;
            double len = Math.hypot(dx, dy);
            if (len > 0) {
                double perpX = (-dy / len) * DOUBLE_ROUTE_OFFSET;
                double perpY = (dx / len) * DOUBLE_ROUTE_OFFSET;

                if (route.getId().endsWith("b")) {
                    perpX = -perpX;
                    perpY = -perpY;
                }
                x1 += perpX;
                y1 += perpY;
                x2 += perpX;
                y2 += perpY;
            }
        }

        int ix1 = (int) x1;
        int iy1 = (int) y1;
        int ix2 = (int) x2;
        int iy2 = (int) y2;

        if (route == selectedRoute) {
            g2.setColor(new Color(255, 220, 0));
            g2.setStroke(new BasicStroke(10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(ix1, iy1, ix2, iy2);
        }

        Color lineColor;
        float lineWidth;
        if (route.isRouteClaimed()) {
            lineColor = route.getClaimedBy().getPlayerColor();
            lineWidth = 6f;
        } else {
            lineColor = routeColorToAwt(route.getRouteColor());
            lineWidth = 3.5f;
        }

        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(ix1, iy1, ix2, iy2);

        if (!route.isRouteClaimed()) {
            int midX = (ix1 + ix2) / 2;
            int midY = (iy1 + iy2) / 2;
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(getFont().deriveFont(9f));
            g2.drawString(String.valueOf(route.getRouteLength()), midX + 3, midY - 3);
        }
    }

    private void drawCities(Graphics2D g2, BoardMap board) {
        for (City city : board.getAllCities()) {
            drawCity(g2, city);
        }
    }

    private void drawCity(Graphics2D g2, City city) {
        int cx = city.getPixelX(getWidth());
        int cy = city.getPixelY(getHeight());

        g2.setColor(new Color(60, 30, 20));
        g2.fillOval(cx - CITY_RADIUS, cy - CITY_RADIUS, CITY_RADIUS * 2, CITY_RADIUS * 2);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - CITY_RADIUS, cy - CITY_RADIUS, CITY_RADIUS * 2, CITY_RADIUS * 2);

        g2.setColor(Color.BLACK);
        g2.setFont(getFont().deriveFont(9f));
        FontMetrics fm = g2.getFontMetrics();
        String label = city.getName();
        int labelX = cx - fm.stringWidth(label) / 2;
        int labelY = cy + CITY_RADIUS + 11;
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillRect(labelX - 2, labelY - fm.getAscent(), fm.stringWidth(label) + 4,
                fm.getAscent() + fm.getDescent());
        g2.setColor(Color.BLACK);
        g2.drawString(label, labelX, labelY);
    }

    private Color routeColorToAwt(Route.RouteColor routeColor) {
        switch (routeColor) {
            case RED:    return new Color(200, 50,  50);
            case BLUE:   return new Color(50,  100, 200);
            case GREEN:  return new Color(30,  150, 30);
            case YELLOW: return new Color(200, 180, 0);
            case BLACK:  return new Color(40,  40,  40);
            case WHITE:  return new Color(210, 210, 210);
            case ORANGE: return new Color(220, 110, 20);
            case PINK:   return new Color(210, 80,  150);
            case GREY:   return new Color(150, 150, 150);
            default:     return Color.GRAY;
        }
    }

    public Route getSelectedRoute() {
        return selectedRoute;
    }

    public void clearSelection() {
        selectedRoute = null;
        repaint();
    }

    @Override
    public void onGameStateChange() {
        repaint();
    }
}

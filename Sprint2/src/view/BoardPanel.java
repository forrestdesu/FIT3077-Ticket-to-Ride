package src.view;

import src.interfaces.GameObserver;
import src.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardPanel extends JPanel implements GameObserver {
    private static final int CITY_RADIUS = 10;
    private static final int CLICK_THRESHOLD = 20;
    private static final int DOUBLE_ROUTE_OFFSET = 5;

    private final GameEngine gameEngine;
    private Route selectedRoute;

    public BoardPanel(GameEngine gameEngine){
        this.gameEngine = gameEngine;
        gameEngine.addObserver(this);
        setBorder(BorderFactory.createTitledBorder("Board"));
        setBackground(Color.LIGHT_GRAY);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private int drawX(){
        return getInsets().left + 10;
    }

    private int drawY(){
        return getInsets().top + 10;
    }

    private int drawW(){
        Insets insets = getInsets();
        return getWidth() - insets.left - insets.right - 20;
    }

    private int drawH(){
        Insets insets = getInsets();
        return getHeight() - insets.top - insets.bottom - 20;
    }

    private int toPixelX(City city){
        return drawX() + (int) (city.getxPercent() / 100.0 * drawW());
    }

    private int toPixelY(City city){
        return drawY() + (int) (city.getyPercent() / 100.0 * drawH());
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
        int x1 = toPixelX(route.getCity1());
        int y1 = toPixelY(route.getCity1());
        int x2 = toPixelX(route.getCity2());
        int y2 = toPixelY(route.getCity2());

        if(route instanceof DoubleRoute){
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

        double x1 = toPixelX(route.getCity1());
        double y1 = toPixelY(route.getCity1());
        double x2 = toPixelX(route.getCity2());
        double y2 = toPixelY(route.getCity2());

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
            lineWidth = 7f;
        } else {
            lineColor = routeColorToAwt(route.getRouteColor());
            lineWidth = 4f;
        }

        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(ix1, iy1, ix2, iy2);

        if (!route.isRouteClaimed()) {
            int midX = (ix1 + ix2) / 2;
            int midY = (iy1 + iy2) / 2;
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            String lenStr = String.valueOf(route.getRouteLength());
            int lw = fm.stringWidth(lenStr);
            g2.setColor(new Color(255, 255, 255, 190));
            g2.fillRoundRect(midX - lw / 2 - 2, midY - fm.getAscent() - 1, lw + 4, fm.getAscent() + fm.getDescent() + 1, 3, 3);
            g2.setColor(new Color(40, 40, 40));
            g2.drawString(lenStr, midX - lw / 2, midY);
        }else{
            drawClaimedRoutePlayer(g2, ix1, iy1, ix2, iy2, route.getClaimedBy().getPlayerName());
        }
    }

    private void drawClaimedRoutePlayer(Graphics2D g2, int x1, int y1, int x2, int y2, String playerName) {
        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        g2.setFont(new Font("Arial", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(playerName);
        int textH = fm.getAscent() + fm.getDescent();

        int padX = 4;
        int padY = 2;
        int pillX = midX - textW / 2 - padX;
        int pillY = midY - fm.getAscent() - padY;
        int pillW = textW + padX * 2;
        int pillH = textH + padY * 2;

        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 6, 6);

        g2.setColor(new Color(80, 80, 80, 180));
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 6, 6);

        g2.setColor(new Color(20, 20, 20));
        g2.drawString(playerName, midX - textW / 2, midY + fm.getDescent() - 1);
    }

    private void drawCities(Graphics2D g2, BoardMap board) {
        for (City city : board.getAllCities()) {
            drawCity(g2, city);
        }
    }

    private void drawCity(Graphics2D g2, City city) {
        int cx = toPixelX(city);
        int cy = toPixelY(city);

        g2.setColor(new Color(60, 30, 20));
        g2.fillOval(cx - CITY_RADIUS, cy - CITY_RADIUS, CITY_RADIUS * 2, CITY_RADIUS * 2);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - CITY_RADIUS, cy - CITY_RADIUS, CITY_RADIUS * 2, CITY_RADIUS * 2);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        String label = city.getName();
        int labelX = cx - fm.stringWidth(label) / 2;
        int labelY = cy + CITY_RADIUS + 12;
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(labelX - 3, labelY - fm.getAscent() - 1, fm.stringWidth(label) + 6, fm.getAscent() + fm.getDescent() + 2, 4, 4);
        g2.setColor(new Color(30, 20, 10));
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
    public void onGameStateChanged() {
        repaint();
    }
}

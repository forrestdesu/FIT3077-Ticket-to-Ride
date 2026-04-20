package src.util;

import src.model.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDataHandler {
    private static final String MAP_FILE = "src/assets/board_data.json";

    public BoardMap loadBoardMap(){
        String json = readFile();
        BoardMap boardMap = new BoardMap();

        Map<String, City> cityById = new HashMap<>();
        String locationsBlock = extractArrayBlock(json, "locations");

        for(String object : splitObjects(locationsBlock)){
            String id = extractString(object, "id");
            String name = extractString(object, "name");
            double x = extractDouble(object, "x");
            double y = extractDouble(object, "y");
            City city = new City(id, name, x, y);
            boardMap.addCity(city);
            cityById.put(id, city);
        }

        String routeBlock = extractArrayBlock(json, "routes");
        List<String> routeObjects = splitObjects(routeBlock);

        List<DoubleRoute[]> doublePairs = new ArrayList<>();

        int routeIndex = 0;
        for(String object : routeObjects){
            String city1Id = extractString(object, "city1");
            String city2Id = extractString(object, "city2");
            String colorString = extractString(object, "color");
            int length = extractInt(object, "length");
            boolean isDouble = extractBoolean(object, "isDouble");

            City city1 = cityById.get(city1Id);
            City city2 = cityById.get(city2Id);
            if(city1 == null || city2 == null){
                throw new RuntimeException("Route reference unknown city id: " + city1Id + " or " + city2Id);
            }

            Route.RouteColor color = parseRouteColor(colorString);

            if(isDouble){
                String idA = "r" + routeIndex + "a";
                String idB = "r" + routeIndex + "b";

                Route.RouteColor colorB = parseSecondRouteColor(colorString, color);

                DoubleRoute sideA = new DoubleRoute(idA, city1, city2, length, color);
                DoubleRoute sideB = new DoubleRoute(idB, city1, city2, length, colorB);

                sideA.setParallelRoute(sideB);
                sideB.setParallelRoute(sideA);

                boardMap.addRoute(sideA);
                boardMap.addRoute(sideB);
                doublePairs.add(new DoubleRoute[]{sideA, sideB});
            }else{
                String id = "r" + routeIndex;
                SingleRoute route = new SingleRoute(id, city1, city2, length, color);
                boardMap.addRoute(route);
            }
            routeIndex++;
        }
        return boardMap;
    }

    public TicketDeck loadTicketDeck(){
        String json = readFile();

        Map<String, String> idToName = new HashMap<>();
        String locationsBlock = extractArrayBlock(json, "locations");
        for(String object : splitObjects(locationsBlock)){
            String id = extractString(object, "id");
            String name = extractString(object,  "name");
            idToName.put(id, name);
        }

        List<DestinationTicket> tickets = new ArrayList<>();
        String ticketsBlock = extractArrayBlock(json, "destination_tickets");
        for(String object : splitObjects(ticketsBlock)){
            String city1Id = extractString(object, "city1");
            String city2Id = extractString(object, "city2");
            int points = extractInt(object, "points");
            String city1Name = idToName.getOrDefault(city1Id, city1Id);
            String city2Name = idToName.getOrDefault(city2Id, city2Id);
            tickets.add(new DestinationTicket(city1Name, city2Name, points));
        }
        return new TicketDeck(tickets);
    }

    private Route.RouteColor parseRouteColor(String colorString){
        String first = colorString.split("/")[0].trim().toUpperCase();
        if(first.equals("GREY")){
            return Route.RouteColor.GREY;
        }
        try{
            return Route.RouteColor.valueOf(first);
        }catch (IllegalArgumentException e){
            System.err.println("Unknown route color: " + first + " - defaulting to GRAY");
        }
        return Route.RouteColor.GREY;
    }

    private Route.RouteColor parseSecondRouteColor(String colorString, Route.RouteColor fallback){
        String[]  parts = colorString.split("/");
        if(parts.length < 2){
            return fallback;
        }

        String second = parts[1].trim().toUpperCase();
        if(second.equals("GREY")){
            return Route.RouteColor.GREY;
        }
        try{
            return Route.RouteColor.valueOf(second);
        }catch (IllegalArgumentException e){
            return fallback;
        }
    }

    private String readFile(){
        InputStream is = getClass().getClassLoader().getResourceAsStream(MAP_FILE);
        if(is == null){
            throw new RuntimeException("Cannot find map file on classpath: " + MAP_FILE + "\n Make sure 'assets/board_map.json' is in your resources/classpath root.");
        }
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))){
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                sb.append(line).append("\n");
            }
            return sb.toString();
        }catch (Exception e){
            throw new RuntimeException("Failed to read map file: " + e.getMessage());
        }
    }

    private String extractArrayBlock(String json, String key){
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if(keyIdx == -1){
            throw new RuntimeException("JSON array key " + key + " not found in file");
        }
        int bracketOpen = json.indexOf('[', keyIdx);
        int bracketClose = findMatchingSquareBracket(json, bracketOpen);
        return json.substring(bracketOpen + 1, bracketClose);
    }

    private int findMatchingSquareBracket(String json, int openIdx){
        int depth = 0;
        for(int i = openIdx; i < json.length(); i++){
            char c = json.charAt(i);
            if(c == '[' || c == '{'){
                depth++;
            }else if(c == ']' || c == '}'){
                depth--;
                if (depth == 0){
                    return i;
                }
            }
        }
        throw new RuntimeException("Unmatched '[' in JSON at index " + openIdx);
    }

    private List<String> splitObjects(String locationsArray){
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for(int i = 0; i < locationsArray.length(); i++){
            char c = locationsArray.charAt(i);
            if (c == '{'){
                if(depth == 0){
                    start = i;
                }
                depth++;
            }else if(c == '}'){
                depth--;
                if(depth == 0 && start != -1){
                    objects.add(locationsArray.substring(start + 1, i));
                    start = -1;
                }
            }
        }
        return objects;
    }

    public String extractString(String object, String key){
        String search = "\"" + key + "\"";
        int keyIdx = object.indexOf(search);
        if(keyIdx == -1){
            throw new RuntimeException("Key not found: \"" + key + "\" in: " + object.trim());
        }
        int colon = object.indexOf(':', keyIdx);
        int firstQuote = object.indexOf('"', colon + 1);
        int secondQuote = object.indexOf('"', firstQuote + 1);
        return object.substring(firstQuote + 1, secondQuote);
    }

    public int extractInt(String object, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = object.indexOf(search);
        if (keyIdx == -1) {
            throw new RuntimeException("Key not found: \"" + key + "\" in: " + object.trim());
        }
        int colon = object.indexOf(':', keyIdx);
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        for (int i = colon + 1; i < object.length(); i++) {
            char c = object.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
                started = true;
            } else if (started) {
                break;
            }
        }
        return Integer.parseInt(sb.toString());
    }

    private double extractDouble(String obj, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = obj.indexOf(search);
        if (keyIdx == -1) {
            throw new RuntimeException("Key not found: \"" + key + "\" in: " + obj.trim());
        }
        int colon = obj.indexOf(':', keyIdx);
        StringBuilder sb = new StringBuilder();
        boolean started = false;
        for (int i = colon + 1; i < obj.length(); i++) {
            char c = obj.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                sb.append(c);
                started = true;
            } else if (started) {
                break;
            }
        }
        return Double.parseDouble(sb.toString());
    }

    private boolean extractBoolean(String obj, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = obj.indexOf(search);
        if (keyIdx == -1) {
            throw new RuntimeException("Key not found: \"" + key + "\" in: " + obj.trim());
        }
        int colon = obj.indexOf(':', keyIdx);
        // Find the next non-whitespace character after the colon
        for (int i = colon + 1; i < obj.length(); i++) {
            char c = obj.charAt(i);
            if (c == 't') {
                return true;
            } else if (c == 'f') {
                return false;
            }
        }
        throw new RuntimeException("Cannot parse boolean for key: " + key);
    }
}

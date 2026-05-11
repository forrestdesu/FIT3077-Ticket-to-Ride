package src.util;

import src.model.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * First, loadBoardMap() or loadTicketDeck() is called.
 * These are the main methods that start reading the JSON file.
 *
 * Both methods first call readFile() to load the entire JSON file as a String.
 *
 * Then extractArrayBlock() is used to get sections like locations, routes, and destination_tickets from the JSON.
 *
 * findMatchingSquareBracket() helps extractArrayBlock() find where each array ends.
 *
 * splitObjects() then separates each array into individual JSON objects.
 *
 * After that, helper methods such as:
 * - extractString()
 * - extractInt()
 * - extractDouble()
 * - extractBoolean()
 *
 * are used to get values from each object.
 *
 * loadBoardMap() uses these values to create City and Route objects.
 *
 * loadTicketDeck() uses these values to create DestinationTicket objects.
 *
 * Finally, all objects are stored inside BoardMap and TicketDeck and returned.
 */

public class MapDataHandler {
    private static final String MAP_FILE = "src/assets/board_data.json";

    //this is the main method that will loads the board map, this includes the reading of json file to extract all the location, routes and destination tickets
    public BoardMap loadBoardMap(){
        String json = readFile();
        BoardMap boardMap = new BoardMap();

        Map<String, City> cityById = new HashMap<>();

        //extracting all the location as an array of locations
        String locationsBlock = extractArrayBlock(json, "locations");

        //then we are parsing all the locations into the ids, name and xy coordinates
        for(String object : splitObjects(locationsBlock)){
            String id = extractString(object, "id");
            String name = extractString(object, "name");
            double x = extractDouble(object, "x");
            double y = extractDouble(object, "y");
            City city = new City(id, name, x, y);
            boardMap.addCity(city);
            cityById.put(id, city);
        }

        //extracting all the routes  as an array of locations
        String routeBlock = extractArrayBlock(json, "routes");
        List<String> routeObjects = splitObjects(routeBlock);

        List<DoubleRoute[]> doublePairs = new ArrayList<>();

        int routeIndex = 0;
        //then we are parsing all the locations into the city1, city2, color, length and if the route is a double route or not
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
                //handles double routes: two parallel connections between the same cities
                String idA = "r" + routeIndex + "a";
                String idB = "r" + routeIndex + "b";

                Route.RouteColor colorB = parseSecondRouteColor(colorString, color);

                DoubleRoute sideA = new DoubleRoute(idA, city1, city2, length, color);
                DoubleRoute sideB = new DoubleRoute(idB, city1, city2, length, colorB);

                //linking the two routes as parallel
                sideA.setParallelRoute(sideB);
                sideB.setParallelRoute(sideA);

                boardMap.addRoute(sideA);
                boardMap.addRoute(sideB);
                doublePairs.add(new DoubleRoute[]{sideA, sideB});
            }else{
                // if not double we set it as a single route
                String id = "r" + routeIndex;
                SingleRoute route = new SingleRoute(id, city1, city2, length, color);
                boardMap.addRoute(route);
            }
            routeIndex++;
        }
        return boardMap;
    }

    //Loads all destination tickets from the JSON file.
    public TicketDeck loadTicketDeck(){
        String json = readFile();

        // we are mapping Ids to the names
        Map<String, String> idToName = new HashMap<>();
        String locationsBlock = extractArrayBlock(json, "locations");
        for(String object : splitObjects(locationsBlock)){
            String id = extractString(object, "id");
            String name = extractString(object,  "name");
            idToName.put(id, name);
        }

        //parsing the destination tickets
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

    //parsing a route color string, supporting formats like "RED" or "RED/BLUE".
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

    //parsing the second route color for double routes and if it is not specified or invalid, fallback to the first route's color.
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

    //reads the JSON map file from classpath.
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

    //extracts the JSON array string for a named top-level array key. This handles both "locations" and "destination_tickets" keys
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

    //this finds the matching ] for a [ at the given index so that we can separate the array related to keys
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

    //this splits an array's content string into individual json object strings
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

    //this extracts a string value for the given key from a json object fragment
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

    //this extracts an integer value for the given string and its key
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

    //this extracts a double value for the given key and its string object
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

    //Extracts a boolean value for the given key and string object.
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

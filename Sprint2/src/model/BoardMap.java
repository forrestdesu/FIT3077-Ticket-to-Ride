package src.model;

import java.util.*;

public class BoardMap {
    private final Map<String, City> citiesById;
    private final Map<String, City> citiesByName;
    private final Map<String, Route> routesById;
    private final List<Route> allRoutes;

    public BoardMap() {
        this.citiesById = new HashMap<>();
        this.citiesByName = new HashMap<>();
        this.routesById = new HashMap<>();
        this.allRoutes = new ArrayList<>();
    }

    public void addCity(City city){
        citiesById.put(city.getId(), city);
        citiesByName.put(city.getName(), city);
    }

    public void addRoute(Route route){
        routesById.put(route.getId(), route);
        allRoutes.add(route);
    }

    public City getCityById(String id){
        return citiesById.get(id);
    }

    public City getCityByName(String name){
        return citiesByName.get(name);
    }

    public Route getRouteById(String id){
        return routesById.get(id);
    }

    public List<City> getAllCities(){
        return Collections.unmodifiableList(new ArrayList<>(citiesById.values()));
    }

    public List<Route> getAllRoutes(){
        return Collections.unmodifiableList(allRoutes);
    }

    public List<Route> getRoutesForCity(String cityName){
        List<Route> resultRoutes = new ArrayList<>();

        for (Route route : allRoutes){
            if(route.connectsTo(cityName)){
                resultRoutes.add(route);
            }
        }
        return resultRoutes;
    }
}

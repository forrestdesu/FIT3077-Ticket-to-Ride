package src.model;

public class DoubleRoute extends Route{
    private DoubleRoute parallelRoute;

    public DoubleRoute(String id, City city1, City city2, int routeLength, RouteColor routeColor){
        super(id, city1, city2, routeLength, routeColor);
    }

    public void setParallelRoute(DoubleRoute parallelRoute){
        this.parallelRoute = parallelRoute;
    }

    public DoubleRoute getParallelRoute(){
        return parallelRoute;
    }

    public boolean isDoubleRouteAvailableToPlayer(){
        return isRouteClaimed() ? false : parallelRoute != null && parallelRoute.isRouteClaimed() ? false : true;
    }
}

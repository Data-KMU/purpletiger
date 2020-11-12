package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.Waypoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

public class PointMatcher extends Matcher {


    private final float longitude;
    private final float latitude;
    private final Float altitude;

    private Point point;

    public PointMatcher(float longitude, float latitude, Float altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    @Override
    public void initialize() {
        point = Matcher.geometryFactory.createPoint(
                new Coordinate(longitude, latitude)
        );
    }

    @Override
    protected boolean calculate(Area area) {
        Coordinate[] currentCoords = AreaMatcher.areaToGeoCoordinates(area);
        float currentElevation = area.getElevation();
        float currentHeight = area.getHeight();

        if (altitude != null && (altitude < currentElevation || altitude > currentElevation + currentHeight)) {
            return false;
        }

        if (currentCoords.length >= 4) {
            return point.within(
                    Matcher.geometryFactory.createPolygon(currentCoords)
            );
        }

        return false;
    }

    /*
     * calculate the nearest Waypoint and its distance to the given Point
     */
    @Override
    protected boolean calculate(Corridor corridor) {
        double currentDistance;
        Coordinate[] currentCords = corridorToGeoCoordinates(corridor);
        double newMinDistance = calcDistancePytagoras(currentCords[0]);
        Waypoint nearestWaypoint;

        for (int i = 0; i < currentCords.length; i++) {
            currentDistance = calcDistancePytagoras(currentCords[i]);
            if (currentDistance < newMinDistance) {
                newMinDistance = currentDistance;
                nearestWaypoint = corridor.getCoordinates().get(i);
            }
        }

        //TODO: Radius des nearestWaypoint berechnen und mit newMinDistance vergleichen

        return false;
    }

    public double calcDistancePytagoras(Coordinate coordinate) {
        double x = coordinate.x, y = coordinate.y, z = coordinate.z;
        return Math.pow(Math.pow(x - this.longitude, 2) + Math.pow(y - this.latitude, 2) + Math.pow(z + this.altitude, 2), 0.5);
    }

    public static Coordinate[] corridorToGeoCoordinates(Corridor corridor) {
        int size = corridor.getCoordinates().size();
        Coordinate[] coordinates = new Coordinate[size];
        Waypoint current;
        for (int i = 0; i < size; i++) {
            current = corridor.getCoordinates().get(i);
            coordinates[i] = new Coordinate(current.getLongitude(), current.getLatitude(), current.getAltitude());
        }
        return coordinates;
    }
}

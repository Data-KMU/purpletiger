package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class PointMatcher extends Matcher {


    protected final float longitude;
    protected final float latitude;
    protected final Float altitude;

    protected Point point;

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

    @Override
    protected boolean calculate(Corridor corridor) {
        Coordinate[] coordinates = corridorToGeoCoordinates(corridor);
        LineString lineStringCorridor = Matcher.geometryFactory.createLineString(coordinates);
        double distanceToPoint = lineStringCorridor.distance(point);
        int distanceInMeters = CorridorMatcher.getDistanceInMeters(corridor, point);
        int widthCorridor = CorridorMatcher.getWidthOfNearestWaypoint(corridor, point);

        return (distanceToPoint <= 0 || distanceInMeters < widthCorridor);
    }
}

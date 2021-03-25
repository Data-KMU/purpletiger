package at.taaja.purpletiger.strategies;

import io.taaja.models.raw.SensorData;
import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static at.taaja.purpletiger.strategies.SpatialEntityMatcher.areaToGeoCoordinates;

public class SphereMatcher extends PointMatcher {

    GeometricShapeFactory gsf;
    protected final float accuracy;
    protected Polygon sphere;

    public SphereMatcher(float longitude, float latitude, Float altitude, float accuracy) {
        super(longitude, latitude, altitude);
        this.accuracy = accuracy;
    }
    public SphereMatcher(SensorData sensorData) {
        super(sensorData.getLongitude(), sensorData.getLatitude(), sensorData.getAltitude());
        this.accuracy = sensorData.getDeviation();
    }

    @Override
    public void initialize() {
        gsf = new GeometricShapeFactory();
        gsf.setCentre(new Coordinate(longitude,latitude));
        gsf.setSize(accuracy);
        sphere =  gsf.createCircle();
    }

    @Override
    protected boolean calculate(Area area) {
        Coordinate[] coordinates = areaToGeoCoordinates(area);
        Float currentElevation = area.getElevation();
        Float currentHeight = area.getHeight();

        if (altitude + accuracy < currentElevation || altitude > currentElevation + currentHeight) return false;

        if (coordinates.length >= 4) {
            Polygon poly2 = Matcher.geometryFactory.createPolygon(coordinates);
            return (sphere.overlaps(poly2) || sphere.within(poly2) || poly2.within(sphere));
        }
        return false;
    }

    @Override
    protected boolean calculate(Corridor corridor) {
        Coordinate[] coordinates = corridorToGeoCoordinates(corridor);
        LineString lineStringCorridor = Matcher.geometryFactory.createLineString(coordinates);
        List<Float> elevations = new ArrayList<>();
        for (int i = 0; i < corridor.getCoordinates().size(); i++) {
            elevations.add(corridor.getCoordinates().get(i).getAltitude());
        }
        Float elevationMin = Collections.min(elevations);
        Float elevationMax = Collections.max(elevations);

        int distanceInMeters = CorridorMatcher.getDistanceInMeters(corridor, sphere);
        int widthCorridor = CorridorMatcher.getWidthOfNearestWaypoint(corridor, sphere);

        if (elevationMax < altitude || elevationMin > (altitude + accuracy)) return false;
        if (coordinates.length >= 4) {
            return (sphere.intersects(lineStringCorridor) || distanceInMeters < widthCorridor);
        }
        return false;
    }
}

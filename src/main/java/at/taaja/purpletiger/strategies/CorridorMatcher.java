package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.Waypoint;
import org.apache.commons.lang3.NotImplementedException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class CorridorMatcher extends SpatialEntityMatcher<Corridor> {


    private LineString lineString;
    private Float elevationMin;
    private Float elevationMax;
    private Float radius;

    public CorridorMatcher(Corridor corridor) {
        super(corridor);
    }

    @Override
    public void initialize() {
        Coordinate[] coordinates = corridorToGeoCoordinates(this.spatialEntity);
        this.lineString = Matcher.geometryFactory.createLineString(coordinates);
        List<Float> elevations = new ArrayList<>();
        for (int i = 0; i < this.spatialEntity.getCoordinates().size(); i++) {
            elevations.add(this.spatialEntity.getCoordinates().get(i).getAltitude());
        }
        this.elevationMin = Collections.min(elevations);
        this.elevationMax = Collections.max(elevations);
    }

    @Override
    protected boolean calculate(Area area) {
        Coordinate[] coordinates = areaToGeoCoordinates(area);
        Polygon poly = Matcher.geometryFactory.createPolygon(coordinates);
        Float areaElevation = area.getElevation(), areaHeight = area.getHeight();

        if (elevationMax < areaElevation || elevationMin > (areaElevation + areaHeight)) return false;

        if (coordinates.length >= 4) {
            return (poly.intersects(lineString));
        }
        return false;
    }

    @Override
    protected boolean calculate(Corridor corridor) {
        Coordinate[] coordinates = corridorToGeoCoordinates(corridor);
        LineString lineStringCorridor = Matcher.geometryFactory.createLineString(coordinates);
        double distanceToPoint = lineStringCorridor.distance(lineString);
        Float radiusCorridor;

        //TODO implement radius
        //if(distanceToPoint<(radius + radiusCorridor))

        return false;
    }

}

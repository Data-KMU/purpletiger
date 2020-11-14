package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.LongLat;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AreaMatcher extends SpatialEntityMatcher<Area> {

    private Polygon poly;
    private Float elevation;
    private Float height;

    public AreaMatcher(Area area) {
        super(area);
    }

    @Override
    public void initialize() {
        this.poly = Matcher.geometryFactory.createPolygon(
                areaToGeoCoordinates(this.spatialEntity)
        );
        this.elevation = spatialEntity.getElevation();
        this.height = spatialEntity.getHeight();
    }

    @Override
    protected boolean calculate(Area area) {
        Coordinate[] coordinates = areaToGeoCoordinates(area);
        Float currentElevation = area.getElevation();
        Float currentHeight = area.getHeight();

        if (elevation + height < currentElevation || elevation > currentElevation + currentHeight) return false;

        if (coordinates.length >= 4) {
            Polygon poly2 = Matcher.geometryFactory.createPolygon(coordinates);
            return (poly.overlaps(poly2) || poly.within(poly2) || poly2.within(poly));
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

        if (elevationMax < elevation || elevationMin > (elevation + height)) return false;

        if (coordinates.length >= 4) {
            return (poly.intersects(lineStringCorridor));
        }
        return false;
    }
}

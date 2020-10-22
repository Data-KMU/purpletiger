package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.LongLat;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

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

        Coordinate[] currentCoords = areaToGeoCoordinates(area);
        Float currentElevation = area.getElevation();
        Float currentHeight = area.getHeight();

        //check if Areas overlap vertical
        if (elevation + height < currentElevation || elevation > currentElevation + currentHeight) return false;

        //check if Entity is Polygon (Error below 4 points)
        if (currentCoords.length >= 4) {
            Polygon poly2 = Matcher.geometryFactory.createPolygon(currentCoords);
            return  (poly.overlaps(poly2) || poly.within(poly2) || poly2.within(poly));
        }

        return false;
    }

    @Override
    protected boolean calculate(Corridor corridor) {
        //todo
        return false;
    }


    public static Coordinate[] areaToGeoCoordinates(Area area) {
        Coordinate[] coords = new Coordinate[area.getCoordinates().get(0).size()];
        int i = 0;
        for (LongLat ll : area.getCoordinates().get(0)) {
            coords[i] = new Coordinate(ll.getLongitude(), ll.getLatitude());
            i++;
        }
        return coords;
    }
}

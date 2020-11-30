package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.LongLat;
import io.taaja.models.record.spatial.SpatialEntity;
import org.locationtech.jts.geom.Coordinate;

public abstract class SpatialEntityMatcher<T extends SpatialEntity> extends Matcher {

    protected final T spatialEntity;

    public SpatialEntityMatcher(T spatialEntity) {
        this.spatialEntity = spatialEntity;
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

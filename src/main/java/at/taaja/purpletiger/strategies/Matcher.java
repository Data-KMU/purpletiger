package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import javax.ws.rs.WebApplicationException;

public abstract class Matcher {

    protected static GeometryFactory geometryFactory = new GeometryFactory();

    public void initialize() {
        //override me
    }

    public final boolean intersects(SpatialEntity spatialEntity) {
        if (spatialEntity instanceof Area) {
            return this.calculate((Area) spatialEntity);
        } else if (spatialEntity instanceof Corridor) {
            return this.calculate((Corridor) spatialEntity);
        } else {
            throw new WebApplicationException("unknown type: " + spatialEntity.getClass().getSimpleName());
        }
    }

    protected abstract boolean calculate(Area area);

    protected abstract boolean calculate(Corridor corridor);

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

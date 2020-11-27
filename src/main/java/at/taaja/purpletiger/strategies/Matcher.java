package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.SpatialEntity;
import io.taaja.models.record.spatial.Waypoint;
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
        Waypoint currentWaypoint;
        for (int i = 0; i < size; i++) {
            currentWaypoint = corridor.getCoordinates().get(i);
            if (currentWaypoint.getAltitude() != null) {
                coordinates[i] = new Coordinate(currentWaypoint.getLongitude(), currentWaypoint.getLatitude(), currentWaypoint.getAltitude());
            } else {
                coordinates[i] = new Coordinate(currentWaypoint.getLongitude(), currentWaypoint.getLatitude(), 0.0);
            }
        }
        return coordinates;
    }


}

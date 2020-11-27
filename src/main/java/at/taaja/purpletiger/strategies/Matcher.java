package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.*;
import lombok.SneakyThrows;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.ws.rs.WebApplicationException;
import java.util.LinkedHashMap;

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
            if (current.getAltitude() != null) {
                coordinates[i] = new Coordinate(current.getLongitude(), current.getLatitude(), current.getAltitude());
            } else {
                coordinates[i] = new Coordinate(current.getLongitude(), current.getLatitude(), 0.0);
            }
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

    @SneakyThrows
    public int getDistanceInMeters(Corridor corridor1, Corridor corridor2) {
        Coordinate[] coordinates1 = corridorToGeoCoordinates(corridor1);
        Coordinate[] coordinates2 = corridorToGeoCoordinates(corridor2);
        LineString lineString = geometryFactory.createLineString(coordinates2);

        int minDistanceIndex1 = getMinDistanceIndex(coordinates1,lineString);
        Point nearestPoint1 = geometryFactory.createPoint(coordinates1[minDistanceIndex1]);

        CoordinateReferenceSystem crs;
        crs = CRS.decode("EPSG:4326");
        GeodeticCalculator gc = new GeodeticCalculator(crs);
        gc.setStartingPosition(JTS.toDirectPosition(DistanceOp.nearestPoints(lineString, nearestPoint1 )[0], crs));
        gc.setDestinationPosition(JTS.toDirectPosition(coordinates1[minDistanceIndex1], crs));

        double distance1 = gc.getOrthodromicDistance();
        return (int) distance1;
    }

    public int getWidthOfNearestWaypoint(Corridor corridor1, Corridor corridor2){
        LineString lineString = geometryFactory.createLineString(corridorToGeoCoordinates(corridor2));
        int minDistanceIndex = getMinDistanceIndex(corridorToGeoCoordinates(corridor1),lineString);
        LinkedHashMap<String, Object> parsed = (LinkedHashMap) corridor1.getCoordinates().get(minDistanceIndex).getAdditionalData();
        LinkedHashMap<String, Integer> hash = (LinkedHashMap<String, Integer>) parsed.get("width");

        return hash.get("width");
    }

    public int getMinDistanceIndex(Coordinate [] coordinates1,LineString lineString){
        Point p = geometryFactory.createPoint(coordinates1[0]);
        double newMinDistance = p.distance(lineString);
        int minDistanceIndex = 0;
        double currentDistance;
        for (int i = 0; i < coordinates1.length; i++) {
            p = geometryFactory.createPoint(coordinates1[i]);
            currentDistance = p.distance(lineString);
            if (currentDistance < newMinDistance) {
                newMinDistance = currentDistance;
                minDistanceIndex = i;
            }
        }
        return minDistanceIndex;
    }
}

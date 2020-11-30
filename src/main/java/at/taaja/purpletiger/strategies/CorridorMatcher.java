package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@JBossLog
public class CorridorMatcher extends SpatialEntityMatcher<Corridor> {

    static CoordinateReferenceSystem crs;
    static GeodeticCalculator gc;

    private LineString lineString;
    private Float elevationMin;
    private Float elevationMax;

    public CorridorMatcher(Corridor corridor) {
        super(corridor);
    }

    @Override
    @SneakyThrows
    public void initialize() {
        Coordinate[] coordinates = corridorToGeoCoordinates(this.spatialEntity);
        this.lineString = Matcher.geometryFactory.createLineString(coordinates);
        List<Float> elevations = new ArrayList<>();
        for (int i = 0; i < this.spatialEntity.getCoordinates().size(); i++) {
            if (this.spatialEntity.getCoordinates().get(i).getAltitude() != null)
                elevations.add(this.spatialEntity.getCoordinates().get(i).getAltitude());
        }
        if (!elevations.isEmpty()) {
            this.elevationMin = Collections.min(elevations);
            this.elevationMax = Collections.max(elevations);
        } else {
            this.elevationMin = null;
            this.elevationMax = null;
        }


        //quarkus bug
        //https://github.com/quarkusio/quarkus/issues/12136
        //https://github.com/debrief/debrief/issues/1003
        log.warn("Ignore the following: ");
        crs = CRS.decode("EPSG:4326");
        log.warn("--- stop ignore ---");
        gc = new GeodeticCalculator(crs);

    }

    @Override
    protected boolean calculate(Area area) {
        Coordinate[] coordinates = areaToGeoCoordinates(area);
        Polygon poly = Matcher.geometryFactory.createPolygon(coordinates);
        double areaElevation = area.getElevation(), areaHeight = area.getHeight();
        int distanceInMeters = getDistanceInMeters(this.spatialEntity, poly);
        int widthCorridor = getWidthOfNearestWaypoint(this.spatialEntity, poly);

        if (elevationMax < areaElevation || elevationMin > (areaElevation + areaHeight)) return false;
        if (coordinates.length >= 4) {
            //wenn sich Area & Corridor nicht interescten wird geprüft, ob die Distanz < Width ist, dann würde die Width des Corridors die Area intersecten.
            return (poly.intersects(lineString) || distanceInMeters < widthCorridor);
        }
        return false;
    }

    @Override
    protected boolean calculate(Corridor corridor) {
        Coordinate[] coordinates = corridorToGeoCoordinates(corridor);
        LineString lineStringCorridor = Matcher.geometryFactory.createLineString(coordinates);
        int width1 = getWidthOfNearestWaypoint(corridor, this.spatialEntity);
        int width2 = getWidthOfNearestWaypoint(this.spatialEntity, corridor);
        Coordinate[] coordinates1 = corridorToGeoCoordinates(this.spatialEntity);
        LineString lineStringCorridor1 = Matcher.geometryFactory.createLineString(coordinates1);
        int distance = getDistanceInMeters(corridor, lineStringCorridor1);

        //wenn die Distance zwischen den Corridoren 0 ist, intersecten sie sich.
        //wenn sie größer 0 ist wird gecheckt, ob beide widths zusammen größer als die Distance von den Waypoints ist --> überlappende Radien
        return (lineStringCorridor.distance(lineString) <= 0.0 || (width1 + width2) > distance);
    }

    @SneakyThrows
    public static int getDistanceInMeters(Corridor corridor1, Geometry geometry) {
        Coordinate[] coordinatesCorridor = corridorToGeoCoordinates(corridor1);

        int minDistanceIndex1 = 0;
        minDistanceIndex1 = getMinDistanceIndex(coordinatesCorridor, geometry);
        Point nearestPoint = geometryFactory.createPoint(coordinatesCorridor[minDistanceIndex1]);
        gc.setStartingPosition(JTS.toDirectPosition(DistanceOp.nearestPoints(geometry, nearestPoint)[0], crs));
        gc.setDestinationPosition(JTS.toDirectPosition(coordinatesCorridor[minDistanceIndex1], crs));

        double distance = gc.getOrthodromicDistance();
        return (int) distance;
    }


    public int getWidthOfNearestWaypoint(Corridor corridor1, Corridor corridor2) {
        LineString lineString = geometryFactory.createLineString(corridorToGeoCoordinates(corridor2));
        int minDistanceIndex = getMinDistanceIndex(corridorToGeoCoordinates(corridor1), lineString);
        LinkedHashMap<String, Object> parsed = (LinkedHashMap) corridor1.getCoordinates().get(minDistanceIndex).getAdditionalData();
        return (Integer) parsed.get("width");
    }

    public static int getWidthOfNearestWaypoint(Corridor corridor, Polygon poly) {
        int minDistanceIndex = getMinDistanceIndex(corridorToGeoCoordinates(corridor), poly);
        LinkedHashMap<String, Object> parsed = (LinkedHashMap) corridor.getCoordinates().get(minDistanceIndex).getAdditionalData();
        return (Integer) parsed.get("width");
    }

    public static int getWidthOfNearestWaypoint(Corridor corridor, Point point) {
        int minDistanceIndex = getMinDistanceIndex(corridorToGeoCoordinates(corridor), point);
        LinkedHashMap<String, Object> parsed = (LinkedHashMap) corridor.getCoordinates().get(minDistanceIndex).getAdditionalData();
        return (Integer) parsed.get("width");
    }

    public static int getMinDistanceIndex(Coordinate[] coordinates1, Geometry geometry) {
        Point p = geometryFactory.createPoint(coordinates1[0]);
        double newMinDistance = p.distance(geometry);
        int minDistanceIndex = 0;
        double currentDistance;
        for (int i = 0; i < coordinates1.length; i++) {
            p = geometryFactory.createPoint(coordinates1[i]);
            currentDistance = p.distance(geometry);
            if (currentDistance < newMinDistance) {
                newMinDistance = currentDistance;
                minDistanceIndex = i;
            }
        }
        return minDistanceIndex;
    }


}

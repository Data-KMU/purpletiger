package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import lombok.SneakyThrows;
import lombok.extern.jbosslog.JBossLog;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.DistanceOp;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@JBossLog
public class CorridorMatcher extends SpatialEntityMatcher<Corridor> {

    CoordinateReferenceSystem crs;

    GeodeticCalculator gc;

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
        if(!elevations.isEmpty()) {
            this.elevationMin = Collections.min(elevations);
            this.elevationMax = Collections.max(elevations);
        }else{
            this.elevationMin = null;
            this.elevationMax = null;
        }


        //quarkus bug
        //https://github.com/quarkusio/quarkus/issues/12136
        //https://github.com/debrief/debrief/issues/1003
        log.error("Ignore the following: ");
        crs = CRS.decode("EPSG:4326");
        log.error("--- stop ignore ---");
        gc = new GeodeticCalculator(crs);

    }

    @Override
    protected boolean calculate(Area area) {
        Coordinate[] coordinates = areaToGeoCoordinates(area);
        Polygon poly = Matcher.geometryFactory.createPolygon(coordinates);
        double areaElevation = area.getElevation(), areaHeight = area.getHeight();

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
        int width1 = getWidthOfNearestWaypoint(corridor, this.spatialEntity);
        int width2 = getWidthOfNearestWaypoint(this.spatialEntity, corridor);
        int distance = getDistanceInMeters(corridor, this.spatialEntity);
        return (lineStringCorridor.distance(lineString) <= 0.0 && (width1 + width2) > distance);
    }


    @SneakyThrows
    public int getDistanceInMeters(Corridor corridor1, Corridor corridor2) {
        Coordinate[] coordinates1 = corridorToGeoCoordinates(corridor1);
        Coordinate[] coordinates2 = corridorToGeoCoordinates(corridor2);
        LineString lineString = geometryFactory.createLineString(coordinates2);

        int minDistanceIndex1 = getMinDistanceIndex(coordinates1,lineString);
        Point nearestPoint1 = geometryFactory.createPoint(coordinates1[minDistanceIndex1]);

        gc.setStartingPosition(JTS.toDirectPosition(DistanceOp.nearestPoints(lineString, nearestPoint1 )[0], crs));
        gc.setDestinationPosition(JTS.toDirectPosition(coordinates1[minDistanceIndex1], crs));

        double distance1 = gc.getOrthodromicDistance();
        return (int) distance1;
    }

    public int getWidthOfNearestWaypoint(Corridor corridor1, Corridor corridor2){
        LineString lineString = geometryFactory.createLineString(corridorToGeoCoordinates(corridor2));
        int minDistanceIndex = getMinDistanceIndex(corridorToGeoCoordinates(corridor1),lineString);
        LinkedHashMap<String, Object> parsed = (LinkedHashMap) corridor1.getCoordinates().get(minDistanceIndex).getAdditionalData();
        return (Integer) parsed.get("width");
    }

    public int getMinDistanceIndex(Coordinate[] coordinates1, LineString lineString){
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

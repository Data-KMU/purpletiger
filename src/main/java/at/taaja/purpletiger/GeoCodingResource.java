package at.taaja.purpletiger;


import com.fasterxml.jackson.annotation.JsonView;
import io.quarkus.runtime.StartupEvent;
import io.taaja.models.generic.LocationInformation;
import io.taaja.models.record.spatial.*;
import io.taaja.models.views.SpatialRecordView;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1")
public class GeoCodingResource {

    @Inject
    ExtensionRepository extensionRepository;

    private GeometryFactory geometryFactory;

    void onStart(@Observes StartupEvent ev) {
        this.geometryFactory = new GeometryFactory();
    }


    @GET
    @Path("/encode/position")
    @JsonView({SpatialRecordView.Identity.class})
    public LocationInformation getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude,
            @QueryParam("altitude") Float altitude
    ) {
        LocationInformation locationInformation = new LocationInformation();
        locationInformation.setAltitude(altitude);
        locationInformation.setLatitude(latitude);
        locationInformation.setLongitude(longitude);


        GeometryFactory gf = new GeometryFactory();
        Coordinate pointCoords = new Coordinate(longitude, latitude);
        Point point = gf.createPoint(pointCoords);

        Area currentArea;
        float currentElevation;
        float currentHeight;

        for (SpatialEntity currentSpatialEntity : this.extensionRepository.findAll()) {
            currentArea = (Area) currentSpatialEntity;
            Coordinate[] currentCoords = getCoordinatesFromSpatialEntity(currentArea);
            currentElevation = currentArea.getElevation();
            currentHeight = currentArea.getHeight();

            if (
                    altitude != null &&
                    (altitude < currentElevation || altitude > currentElevation + currentHeight)
            ){
                continue;
            }

            if (currentCoords.length >= 4) {
                Polygon poly = gf.createPolygon(currentCoords);

                if (point.within(poly)) {
                    locationInformation.addSpatialEntity(currentSpatialEntity);

                }
            }
        }
        return locationInformation;
    }

    @GET
    @Path("/encode/position")
    @JsonView({SpatialRecordView.Identity.class})
    public LocationInformation getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude
    ) {
        return this.getPos(longitude, latitude, null);
    }


    @POST
    @Path("/calculate/intersectingExtensions")
    @JsonView({SpatialRecordView.Identity.class})
    @Consumes(MediaType.APPLICATION_JSON)
    public LocationInformation getAffectedSpatialEntities(SpatialEntity spatialEntity) {
        LocationInformation li = new LocationInformation();



        Coordinate[] coords = getCoordinatesFromSpatialEntity(spatialEntity);

        if(coords == null){
            throw new WebApplicationException("Cant retrieve Coordinates from SpatialEntity");
        }

        Polygon polygonToCheck = gf.createPolygon(coords);


        for (SpatialEntity currentSpatialEntity : this.extensionRepository.findAll()) {

            if(spatialEntity instanceof Area) {
                Area area = (Area) spatialEntity;

                float elevation = area.getElevation();
                float currentElevation;
                float height = area.getHeight();
                float currentHeight;


                Area currentArea = (Area) currentSpatialEntity;
                Coordinate[] currentCoords = getCoordinatesFromSpatialEntity(currentArea);

                if(currentCoords == null){
                    continue;
                }

                currentElevation = currentArea.getElevation();
                currentHeight = currentArea.getHeight();

                //check if Areas overlap vertical
                if (elevation + height < currentElevation || elevation > currentElevation + currentHeight) continue;

                //check if Entity is Polygon (Error below 4 points)
                Polygon poly2 = gf.createPolygon(currentCoords);
                if (polygonToCheck.overlaps(poly2) || polygonToCheck.within(poly2) || poly2.within(polygonToCheck)) {
                    li.addSpatialEntity(currentSpatialEntity);
                }


            }else {
                Corridor corridor = (Corridor) spatialEntity;



            }





        }
        return li;
    }

    private Polygon getCoordinatesFromSpatialEntity(SpatialEntity spatialEntity) {

        ArrayList<Coordinate> out = new ArrayList();

        if(spatialEntity instanceof Area){
            Area area = (Area) spatialEntity;

            if(area.getCoordinates() == null || area.getCoordinates().isEmpty() || area.getCoordinates().get(0).size() < 4){
                return null;
            }

            List<LongLat> longLats = area.getCoordinates().get(0);
            longLats.forEach(longLat -> out.add(new Coordinate(longLat.getLongitude(), longLat.getLatitude())));

        }else {

            // create polygon or LineString from Corridor
            Corridor corridor = (Corridor) spatialEntity;

            if(corridor.getCoordinates() == null || corridor.getCoordinates().isEmpty()){
                return null;
            }

            List<Waypoint> waypoints = corridor.getCoordinates();
            waypoints.forEach(waypoint -> out.add(new Coordinate(waypoint.getLongitude(), waypoint.getLatitude())));
        }


        return this.geometryFactory.createPolygon(
                out.toArray(Coordinate[]::new)
        );
    }


}
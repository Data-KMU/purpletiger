package at.taaja.purpletiger;


import com.fasterxml.jackson.annotation.JsonView;
import io.taaja.models.generic.LocationInformation;
import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.LongLat;
import io.taaja.models.record.spatial.SpatialEntity;
import io.taaja.models.views.SpatialRecordView;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1")
public class GeoCodingResource {

    @Inject
    ExtensionRepository extensionRepository;


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
            Coordinate[] currentCoords = toGeoCoords(currentArea);
            currentElevation = currentArea.getElevation();
            currentHeight = currentArea.getHeight();

            if (altitude == null) {
                if (altitude < currentElevation || altitude > currentElevation + currentHeight) continue;
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
    public LocationInformation getAffectedAreas(SpatialEntity spatialEntity) {
        LocationInformation li = new LocationInformation();
        GeometryFactory gf = new GeometryFactory();

        Area area = (Area) spatialEntity;
        Coordinate[] coords = toGeoCoords(area);
        Polygon poly1 = gf.createPolygon(coords);

        Area currentArea;
        float elevation = area.getElevation();
        float currentElevation;
        float height = area.getHeight();
        float currentHeight;

        for (SpatialEntity currentSpatialEntity : this.extensionRepository.findAll()) {
            currentArea = (Area) currentSpatialEntity;
            Coordinate[] currentCoords = toGeoCoords(currentArea);
            currentElevation = currentArea.getElevation();
            currentHeight = currentArea.getHeight();

            //check if Areas overlap vertical
            if (elevation + height < currentElevation || elevation > currentElevation + currentHeight) continue;

            //check if Entity is Polygon (Error below 4 points)
            if (currentCoords.length >= 4) {
                Polygon poly2 = gf.createPolygon(currentCoords);
                if (poly1.overlaps(poly2) || poly1.within(poly2) || poly2.within(poly1)) {
                    li.addSpatialEntity(currentSpatialEntity);
                }
            }
        }
        return li;
    }

    private static Coordinate[] toGeoCoords(Area area) {
        Coordinate[] coords = new Coordinate[area.getCoordinates().get(0).size()];
        int i = 0;
        for (LongLat ll : area.getCoordinates().get(0)) {
            coords[i] = new Coordinate(ll.getLongitude(), ll.getLatitude());
            i++;
        }
        return coords;
    }
}
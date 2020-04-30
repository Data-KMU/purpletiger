package at.taaja.purpletiger;


import com.fasterxml.jackson.annotation.JsonView;
import io.taaja.models.generic.LocationInformation;
import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.SpatialEntity;
import io.taaja.models.views.SpatialRecordView;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/encode")
public class GeoCodingResource {

    @GET
    @Path("/position")
    @JsonView({SpatialRecordView.Identity.class})
    public LocationInformation getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude,
            @QueryParam("altitude") Float altitude
    ) {

        //todo: retrieve SpatialEntity from DB

        LocationInformation locationInformation = new LocationInformation();
        locationInformation.setAltitude(altitude);
        locationInformation.setLatitude(latitude);
        locationInformation.setLongitude(longitude);


        SpatialEntity extension = new Area();
        extension.setId("c56b3543-6853-4d86-a7bc-1cde673a5582");
        locationInformation.getSpatialEntities().add(extension);


        return locationInformation;
    }

    @GET
    @Path("/position")
    @JsonView({SpatialRecordView.Identity.class})
    public LocationInformation getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude
    ) {
        return this.getPos(longitude, latitude, null);
    }


    @POST
    @Path("/intersectingExtensions")
    @JsonView({SpatialRecordView.Identity.class})
    @Consumes(MediaType.APPLICATION_JSON)
    public LocationInformation getAffectedAreas(SpatialEntity spatialEntity) {

        //todo: retrieve SpatialEntity from DB
        LocationInformation locationInformation = new LocationInformation();
        SpatialEntity extension = new Area();
        extension.setId("c56b3543-6853-4d86-a7bc-1cde673a5582");
        locationInformation.getSpatialEntities().add(extension);


        return locationInformation;
    }




}
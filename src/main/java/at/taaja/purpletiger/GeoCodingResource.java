package at.taaja.purpletiger;

import com.fasterxml.jackson.annotation.JsonView;
import io.smallrye.mutiny.Uni;
import io.taaja.models.generic.LocationInformation;
import io.taaja.models.record.spatial.SpatialEntity;
import io.taaja.models.views.SpatialRecordView;
import lombok.extern.jbosslog.JBossLog;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


@Produces(MediaType.APPLICATION_JSON)
@Path("/v1")
@JBossLog
public class GeoCodingResource {

    @Inject
    LocatorService locatorService;

    @GET
    @Path("/encode/position")
    @JsonView({SpatialRecordView.Identity.class})
    public Uni<LocationInformation> getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude,
            @QueryParam("altitude") Float altitude

    ) {

        LocationInformation locationInformation = new LocationInformation();
        locationInformation.setAltitude(altitude);
        locationInformation.setLatitude(latitude);
        locationInformation.setLongitude(longitude);
            //async
            return Uni.createFrom().item(locationInformation).onItem().invoke(li -> {
                li.setSpatialEntities(
                        locatorService.calculateIntersectingEntities(li.getLongitude(), li.getLatitude(), li.getAltitude())
                );
            });
    }

    @GET
    @Path("/encode/position")
    @JsonView({SpatialRecordView.Identity.class})
    public Uni<LocationInformation> getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude
    ) {
        return this.getPos(longitude, latitude, null);
    }


    @POST
    @Path("/calculate/intersectingExtensions")
    @JsonView({SpatialRecordView.Identity.class})
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<LocationInformation> getAffectedAreas(SpatialEntity spatialEntity) {

        return Uni.createFrom().item(spatialEntity)
                .onItem().apply(entity -> {
                    LocationInformation locationInformation = new LocationInformation();
                    locationInformation.setSpatialEntities(
                            this.locatorService.calculateIntersectingEntities(entity)
                    );
                    return locationInformation;
                });
    }

}
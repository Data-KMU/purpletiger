package at.taaja.purpletiger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/encode")
public class GeoCodingResource {

    @GET
    public IdData getPos(
            @QueryParam("longitude") String longitude,
            @QueryParam("latitude") String latitude,
            @QueryParam("altitude") String altitude
    ) {
        IdData idData = new IdData();
        idData.setAltitude(altitude);
        idData.setLatitude(latitude);
        idData.setLongitude(longitude);
        IdData.Extension extension = new IdData.Extension();
        extension.setType(IdData.Extension.Type.Area);
        extension.setUuid("c56b3543-6853-4d86-a7bc-1cde673a5582");
        idData.getExtensions().add(extension);
        return idData;
    }

    @GET
    public IdData getPos(
            @QueryParam("longitude") String longitude,
            @QueryParam("latitude") String latitude
    ) {
        return this.getPos(longitude, latitude, null);
    }
}
package at.taaja.purpletiger;

import io.taaja.models.zoning.Extension;
import io.taaja.models.zoning.LocationInformation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/encode")
public class GeoCodingResource {

    @GET
    public LocationInformation getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude,
            @QueryParam("altitude") Float altitude
    ) {

        LocationInformation locationInformation = new LocationInformation();
        locationInformation.setAltitude(altitude);
        locationInformation.setLatitude(latitude);
        locationInformation.setLongitude(longitude);

        Extension extension = new Extension();
        extension.setType(Extension.ExtensionType.Area);
        extension.setUuid("c56b3543-6853-4d86-a7bc-1cde673a5582");
        locationInformation.getExtensions().add(extension);
        return locationInformation;
    }

    @GET
    public LocationInformation getPos(
            @QueryParam("longitude") float longitude,
            @QueryParam("latitude") float latitude
    ) {
        return this.getPos(longitude, latitude, null);
    }
}
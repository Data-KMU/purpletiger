package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import org.apache.commons.lang3.NotImplementedException;
import org.locationtech.jts.geom.Coordinate;

public class CorridorMatcher extends SpatialEntityMatcher<Corridor> {

    public CorridorMatcher(Corridor corridor) {
        super(corridor);
    }

    @Override
    protected boolean calculate(Area area) {
        //todo
        return false;
    }

    @Override
    protected boolean calculate(Corridor corridor) {
        //todo
        return false;
    }


    public static Coordinate[] toGeoCoords(Corridor corridor) {
        throw new NotImplementedException("todo");
    }

}

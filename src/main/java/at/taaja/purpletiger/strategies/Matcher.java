package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.SpatialEntity;
import org.locationtech.jts.geom.GeometryFactory;

import javax.ws.rs.WebApplicationException;

public abstract class Matcher {

    protected static GeometryFactory geometryFactory = new GeometryFactory();

    public void initialize(){
        //override me
    }

    public final boolean intersects(SpatialEntity spatialEntity){
        if(spatialEntity instanceof Area){
            return this.calculate((Area) spatialEntity);
        }else if(spatialEntity instanceof Corridor) {
            return this.calculate((Corridor) spatialEntity);
        }else {
            throw new WebApplicationException("unknown type: " + spatialEntity.getClass().getSimpleName());
        }
    }

    protected abstract boolean calculate(Area area);

    protected abstract boolean calculate(Corridor corridor);


}

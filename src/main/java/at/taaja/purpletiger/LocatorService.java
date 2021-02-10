package at.taaja.purpletiger;

import at.taaja.purpletiger.strategies.*;
import io.quarkus.runtime.StartupEvent;
import io.taaja.models.generic.Coordinates;
import io.taaja.models.raw.SensorData;
import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import io.taaja.models.record.spatial.SpatialEntity;
import lombok.extern.jbosslog.JBossLog;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@JBossLog
public class LocatorService {

    @Inject
    ExtensionRepository extensionRepository;

    void onStart(@Observes StartupEvent ev) {
        //index extensions
    }


    public List<SpatialEntity> calculateIntersectingEntities(SpatialEntity spatialEntity){

        SpatialEntityMatcher spatialEntityMatcher;
        if(spatialEntity instanceof Area){
            spatialEntityMatcher = new AreaMatcher((Area)spatialEntity);
        }else if(spatialEntity instanceof Corridor) {
            spatialEntityMatcher = new CorridorMatcher((Corridor)spatialEntity);
        }else {
            throw new WebApplicationException("unknown type: " + spatialEntity.getClass().getSimpleName());
        }

        return this.iterate(spatialEntityMatcher);
    }

    public List<SpatialEntity> calculateIntersectingEntities(float longitude, float latitude, Float altitude) {
        return this.iterate(new PointMatcher(longitude, latitude, altitude));
    }

    public List<SpatialEntity> calculateIntersectingEntities(Coordinates coordinates) {
        return this.iterate(new PointMatcher(coordinates.getLongitude(), coordinates.getLatitude(), coordinates.getAltitude()));
    }

    public List<SpatialEntity> calculateIntersectingEntities(SensorData sensorData) {
        return this.iterate(new SphereMatcher(sensorData));
    }


    private List<SpatialEntity> iterate(Matcher m){

        List<SpatialEntity> intersectingEntities = new ArrayList<>();
        try{
            m.initialize();
        }catch (Exception e){
            log.error("cant initialize matcher (" + m.getClass().getSimpleName() + "). Reason: " + e.getMessage(), e);
            return intersectingEntities;
        }

         for (SpatialEntity toCheck : this.extensionRepository.findAll()){
            try {
                if(m.intersects(toCheck)){
                    intersectingEntities.add(toCheck);
                }
            }catch (Exception e){
                log.error("cant process matching (entity id: " + toCheck.getId() + " on matcher " +
                        m.getClass().getSimpleName() + "). Reason: " + e.getMessage(), e);
            }
        }
        return intersectingEntities;
    }

}

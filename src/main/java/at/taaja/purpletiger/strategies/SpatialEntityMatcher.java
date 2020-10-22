package at.taaja.purpletiger.strategies;

import io.taaja.models.record.spatial.SpatialEntity;

public abstract class SpatialEntityMatcher<T extends SpatialEntity> extends Matcher {

    protected final T spatialEntity;

    public SpatialEntityMatcher(T spatialEntity){
        this.spatialEntity = spatialEntity;
    }




}

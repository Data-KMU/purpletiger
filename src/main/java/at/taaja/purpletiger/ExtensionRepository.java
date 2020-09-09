package at.taaja.purpletiger;

import com.mongodb.client.FindIterable;
import io.taaja.AbstractRepository;
import io.taaja.models.record.spatial.SpatialEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExtensionRepository extends AbstractRepository {

    public ExtensionRepository() {
        super("spatialEntity", SpatialEntity.class);
    }

    public FindIterable<SpatialEntity> findAll(){
        return this.getCollection().find();
    }

}

package at.taaja.purpletiger.strategies;

import io.taaja.models.raw.SensorData;
import io.taaja.models.record.spatial.Area;
import io.taaja.models.record.spatial.Corridor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

public class SphereMatcher extends PointMatcher {


    protected final float accuracy;

    public SphereMatcher(float longitude, float latitude, Float altitude, float accuracy) {
        super(longitude, latitude, altitude);
        this.accuracy = accuracy;
    }
    public SphereMatcher(SensorData sensorData) {
        super(sensorData.getLongitude(), sensorData.getLatitude(), sensorData.getAltitude());
        this.accuracy = sensorData.getDeviation();
    }

    @Override
    public void initialize() {

    }

    @Override
    protected boolean calculate(Area area) {

        return false;
    }

    @Override
    protected boolean calculate(Corridor corridor) {

        return false;
    }
}

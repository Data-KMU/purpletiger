package at.taaja.purpletiger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord;
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.kafka.OutgoingKafkaRecordMetadata;
import io.taaja.models.raw.SensorData;
import io.taaja.models.raw.SpatialSensorData;
import io.taaja.models.record.spatial.SpatialEntity;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.reactive.messaging.*;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@JBossLog
@ApplicationScoped
public class KafkaProcessorService {

    @Inject
    LocatorService locatorService;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test Input: {"longitude":11.4,"latitude":43.1,"altitude":100.2,"informationType":"vehicleInformation","created":1611916191363,"accuracy":1.0,"payload":"payload"}
     * Test Output: {"longitude":11.4,"latitude":43.1,"altitude":100.2,"informationType":"vehicleInformation","created":1611916191363,"accuracy":1.0,"payload":"payload", "intersectingSpatialEntities": ["id1", "id2"]}
     *
     * @param kafkaMessage
     * @return
     */
    @Outgoing("raw-spatial-sensor-data")
    @Incoming("raw-sensor-data")
    public String process(Message<String> kafkaMessage) {

        //ack message
        kafkaMessage.ack();
        try {

            IncomingKafkaRecord<String, String> incomingKafkaRecord = (IncomingKafkaRecord) kafkaMessage;

            String rawData = incomingKafkaRecord.getPayload();

            SensorData sensorData = objectMapper.readValue(rawData, SensorData.class);

            if(sensorData.getOriginId() == null){
                sensorData.setOriginId(
                        incomingKafkaRecord.getKey()
                );
            }

            List<SpatialEntity> spatialEntities = this.locatorService.calculateIntersectingEntities(sensorData);

            List<String> intersectingSpatialEntitiesIds = spatialEntities.stream().map(SpatialEntity::getId).collect(Collectors.toCollection(LinkedList::new));

            if(intersectingSpatialEntitiesIds.isEmpty()){
                log.warn("no intersecting entities found!");
            }

            SpatialSensorData spatialSensorData = SpatialSensorData.createSpatialSensorData(
                    intersectingSpatialEntitiesIds,
                    sensorData
            );

            return objectMapper.writeValueAsString(spatialSensorData);

        } catch (JsonProcessingException e) {
            log.warn("cant parse SensorData: " + e.getMessage());
        }

        return null;
    }


}

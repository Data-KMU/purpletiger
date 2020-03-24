package at.taaja.purpletiger;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class IdData {

    @Data
    public static class Extension{

        public enum Type{
            Area("area"),
            Corridor("corridor");

            private final String value;

            Type(String value) {
                this.value = value;
            }

            @JsonValue
            public String getValue() {
                return value;
            }
        }

        private Type type;
        private String uuid;

    }


    List<Extension> extensions;

    float longitude;
    float latitude;

    //nullable
    Float altitude;

    Date created;

    public IdData(){
        this.created = new Date();
        this.extensions = new ArrayList<>();
    }

}

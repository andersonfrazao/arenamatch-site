package br.com.arenamatch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class GoogleGeoDTO {
    private String status;
    private List<Result> results;

    @Data
    public static class Result {
        @JsonProperty("address_components")
        private List<AddressComponent> addressComponents;
        private Geometry geometry;
    }

    @Data
    public static class AddressComponent {
        @JsonProperty("long_name")
        private String longName;

        @JsonProperty("short_name")
        private String shortName;

        private List<String> types;
    }

    @Data
    public static class Geometry {
        private Location location;
    }

    @Data
    public static class Location {
        private Double lat;
        private Double lng;
    }
}

package ru.faust.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import ru.faust.util.DoubleSerializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingAPIResponseDTO(
        @JsonSerialize(using = DoubleSerializer.class) Double lat,
        @JsonSerialize(using = DoubleSerializer.class) Double lon,
        String name,
        String country,
        String state) {
}

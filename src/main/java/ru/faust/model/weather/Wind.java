package ru.faust.model.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Wind(@JsonProperty("speed") Integer speed,
                   @JsonProperty("deg") Double directionAngle) {
}

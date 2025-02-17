package ru.faust.model.weather;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Main(Double temp,
                   @JsonProperty("feels_like") Double feelsLikeTemp,
                   Double humidity) {
}

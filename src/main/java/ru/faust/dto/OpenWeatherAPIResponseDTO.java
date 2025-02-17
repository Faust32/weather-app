package ru.faust.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.With;
import ru.faust.model.weather.Main;
import ru.faust.model.weather.Sys;
import ru.faust.model.weather.Weather;
import ru.faust.model.weather.Wind;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherAPIResponseDTO (Weather[] weather,
                                         Main main,
                                         Wind wind,
                                         Sys sys,
                                         @With String name,
                                         @With Long locationId) {
}

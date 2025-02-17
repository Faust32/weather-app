package ru.faust.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.faust.dto.OpenWeatherAPIResponseDTO;
import ru.faust.model.Location;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserForecastService {

    private static final Logger logger = LoggerFactory.getLogger(UserForecastService.class);

    private final OpenWeatherMapAPIService openWeatherMapAPIService;

    private final SessionService sessionService;

    private final UserLocationsService userLocationsService;

    public List<OpenWeatherAPIResponseDTO> findAllSavedForecast() {
        logger.info("Finding all saved forecast for user {}...", sessionService.getCurrentUserSession().getUser().getUsername());
        List<Location> locations = userLocationsService.findAllSaved();
        List<OpenWeatherAPIResponseDTO> locationsWithWeather = new ArrayList<>();
        for (Location location : locations) {
            OpenWeatherAPIResponseDTO forecast = openWeatherMapAPIService.getForecast(location);
            forecast = forecast.withLocationId(location.getId()).withName(location.getName());
            locationsWithWeather.add(forecast);
        }
        return locationsWithWeather;
    }

}

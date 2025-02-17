package ru.faust.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.faust.dto.OpenWeatherAPIResponseDTO;
import ru.faust.model.Location;


@Service
public class OpenWeatherMapAPIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherMapAPIService.class);

    private final String API_URL;

    private final String API_KEY;

    private final WebClient webClient;

    public OpenWeatherMapAPIService(WebClient webClient, Environment environment) {
        API_URL = environment.getProperty("open-weather-map.weather.api.url");
        API_KEY = environment.getProperty("open-weather-map.api.key");
        this.webClient = webClient;
    }

    public OpenWeatherAPIResponseDTO getForecast(Location location) {
        logger.info("Getting forecast for city {}", location.getName());
        String url = API_URL + "?lat=" + location.getLatitude() + "&lon=" + location.getLongitude() + "&appid=" + API_KEY + "&units=metric";
        return getWeatherResponse(url);
    }

    private OpenWeatherAPIResponseDTO getWeatherResponse(String url) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(responseBody -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        OpenWeatherAPIResponseDTO weatherConditions = objectMapper.readValue(responseBody, OpenWeatherAPIResponseDTO.class);
                        return Mono.just(weatherConditions);
                    } catch (Exception e){
                        logger.error("Failed to parse weather forecast response from URL: {}. Error: {}", url, e.getMessage(), e);
                        return Mono.error(e);
                    }
                }).block();
    }
}

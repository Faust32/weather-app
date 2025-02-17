package ru.faust.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.faust.dto.GeocodingAPIResponseDTO;

import java.util.List;

@Service
public class GeocodingAPIService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingAPIService.class.getName());

    private final String API_URL;

    private final String API_KEY;

    private final WebClient webClient;

    public GeocodingAPIService(WebClient webClient, Environment environment) {
        API_URL = environment.getProperty("open-weather-map.geocoding.api.url");
        API_KEY = environment.getProperty("open-weather-map.api.key");
        this.webClient = webClient;
    }


    public List<GeocodingAPIResponseDTO> getLocations(String cityName) {
        String url = API_URL + "?q=" + cityName + "&appId=" + API_KEY + "&limit=5";
        logger.info("Getting response from API about location: {}", cityName);
        return getLocationResponse(url);
    }

    private List<GeocodingAPIResponseDTO> getLocationResponse(String url) {
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .<List<GeocodingAPIResponseDTO>>handle((responseBody, sink) -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        sink.next(objectMapper.readValue(responseBody, new TypeReference<>() {
                        }));
                    } catch (Exception e) {
                        logger.error("Failed to parse location response from URL: {}. Error: {}", url, e.getMessage(), e);
                        sink.error(new RuntimeException(e));
                    }
                })
                .block();
    }
}

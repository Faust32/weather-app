package service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.faust.dto.OpenWeatherAPIResponseDTO;
import ru.faust.model.Location;
import ru.faust.service.OpenWeatherMapAPIService;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Transactional
public class OpenWeatherMapAPIServiceTest {

    private MockWebServer mockWebServer;

    private OpenWeatherMapAPIService openWeatherMapAPIService;

    private Location location;

    @BeforeEach
    void setUp() throws IOException {

        location = new Location();
        location.setLongitude(82.9234509);
        location.setLatitude(55.0282171);
        location.setName("novosibirsk");

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        Environment environment = mock(Environment.class);

        when(environment.getProperty("open-weather-map.weather.api.url"))
                .thenReturn(mockWebServer.url("/").url().toString());
        when(environment.getProperty("open-weather-map.api.key"))
                .thenReturn("test-api-key");

        WebClient webClient = WebClient.create();
        openWeatherMapAPIService = new OpenWeatherMapAPIService(webClient, environment);
    }

    @Test
    void getForecast_Success() throws InterruptedException {
        String jsonResponse = """
                {
                  "coord": {
                    "lon": 82.9209,
                    "lat": 55.0271
                  },
                  "weather": [
                    {
                      "id": 600,
                      "main": "Snow",
                      "description": "light snow",
                      "icon": "13d"
                    }
                  ],
                  "base": "stations",
                  "main": {
                    "temp": -3.28,
                    "feels_like": -9.02,
                    "temp_min": -3.28,
                    "temp_max": -3.28,
                    "pressure": 1024,
                    "humidity": 80,
                    "sea_level": 1024,
                    "grnd_level": 1005
                  },
                  "visibility": 10000,
                  "wind": {
                    "speed": 5,
                    "deg": 210
                  },
                  "snow": {
                    "1h": 0.17
                  },
                  "clouds": {
                    "all": 100
                  },
                  "dt": 1739174429,
                  "sys": {
                    "type": 1,
                    "id": 8958,
                    "country": "RU",
                    "sunrise": 1739152927,
                    "sunset": 1739186586
                  },
                  "timezone": 25200,
                  "id": 1496747,
                  "name": "Novosibirsk",
                  "cod": 200
                }
                """;
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonResponse)
        );


        OpenWeatherAPIResponseDTO result = openWeatherMapAPIService.getForecast(location);
        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/?lat=55.0282171&lon=82.9234509&appid=test-api-key&units=metric");

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Novosibirsk");
        assertThat(result.sys().country()).isEqualTo("RU");
        assertThat(result.main().temp()).isEqualTo(-3.28);
        assertThat(result.weather()[0].description()).isEqualTo("light snow");
    }

    @Test
    void getForecast_ifJsonInvalid() {
        String jsonResponse = "{ invalid }";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonResponse)
        );

        assertThrows(RuntimeException.class, () -> openWeatherMapAPIService.getForecast(location));
    }

    @Test
    void getForecast_ifEmpty() {
        String jsonResponse = "{}";

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonResponse)
        );
        location.setLongitude(7712.3);
        location.setLatitude(12.34);
        OpenWeatherAPIResponseDTO result = openWeatherMapAPIService.getForecast(location);

        assertThat(result).isNotNull();
        assertThat(result.weather()).isNull();
        assertThat(result.main()).isNull();
        assertThat(result.wind()).isNull();
        assertThat(result.sys()).isNull();
        assertThat(result.name()).isNull();
        assertThat(result.locationId()).isNull();
    }



    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }
}

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
import ru.faust.dto.GeocodingAPIResponseDTO;
import ru.faust.service.GeocodingAPIService;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
@Transactional
public class GeocodingAPIServiceTest {

    private MockWebServer mockWebServer;

    private GeocodingAPIService geocodingAPIService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        Environment environment = mock(Environment.class);

        when(environment.getProperty("open-weather-map.geocoding.api.url"))
                .thenReturn(mockWebServer.url("/").url().toString());
        when(environment.getProperty("open-weather-map.api.key"))
                .thenReturn("test-api-key");

        WebClient webClient = WebClient.create();
        geocodingAPIService = new GeocodingAPIService(webClient, environment);
    }

    @Test
    void getLocation_success() throws InterruptedException {
        String jsonResponse = """
                [
                    {
                        "name":"Novosibirsk",
                        "lat":55.0282171,
                        "lon":82.9234509,
                        "country":"RU",
                        "state":"Novosibirsk Oblast"
                        }
                ]
                """;
        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(jsonResponse)
        );
        GeocodingAPIResponseDTO result = geocodingAPIService.getLocations("Novosibirsk").getFirst();
        RecordedRequest request = mockWebServer.takeRequest();

        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).isEqualTo("/?q=Novosibirsk&appId=test-api-key&limit=5");

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Novosibirsk");
        assertThat(result.lat()).isEqualTo(55.0282171);
        assertThat(result.lon()).isEqualTo(82.9234509);
        assertThat(result.country()).isEqualTo("RU");
        assertThat(result.state()).isEqualTo("Novosibirsk Oblast");
    }

    @Test
    void getLocation_ifJsonInvalid() {
        String jsonResponse = "{ invalid }";

        mockWebServer.enqueue(
                new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonResponse)
        );

        assertThrows(RuntimeException.class, () -> geocodingAPIService.getLocations("Novosibirsk"));
    }

    @Test
    void getLocation_ifEmpty() {
        String jsonResponse = "[]";

        mockWebServer.enqueue(
                new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(jsonResponse)
        );
        List<GeocodingAPIResponseDTO> result = geocodingAPIService.getLocations("InvalidCityNameBlaBlaBla");

        assertThat(result).isEqualTo(List.of());
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

}

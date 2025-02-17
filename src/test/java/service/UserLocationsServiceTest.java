package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.faust.dto.GeocodingAPIResponseDTO;
import ru.faust.exception.AlreadyExistsException;
import ru.faust.exception.NotFoundModelException;
import ru.faust.model.Location;
import ru.faust.model.Session;
import ru.faust.model.User;
import ru.faust.repository.LocationRepository;
import ru.faust.repository.UserRepository;
import ru.faust.service.SessionService;
import ru.faust.service.UserLocationsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
public class UserLocationsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private UserLocationsService userLocationsService;

    private User user;
    private Location location;
    private Session session;


    @BeforeEach
    void setupUserAndLocation() {
        session = new Session();
        user = new User();
        user.setId(1L);
        user.setLocations(new ArrayList<>());
        session.setUser(user);

        location = new Location();
        location.setId(1L);
        location.setLatitude(12.123);
        location.setLongitude(12.123);
        location.setUsersId(new ArrayList<>());
    }

    @Test
    void save_Success() {

        when(sessionService.getCurrentUserSession()).thenReturn(session);
        when(locationRepository.findByLongitudeAndLatitude(location.getLongitude(), location.getLatitude()))
                .thenReturn(Optional.of(location));

        userLocationsService.save(new GeocodingAPIResponseDTO(12.123, 12.123, "blabla", "bla", "bla"));

        verify(locationRepository).save(location);
        verify(userRepository).save(user);
    }

    @Test
    void save_ifAlreadyExists() {
        location.getUsersId().add(user);

        when(sessionService.getCurrentUserSession()).thenReturn(session);
        when(locationRepository.findByLongitudeAndLatitude(location.getLongitude(), location.getLatitude()))
                .thenReturn(Optional.of(location));

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class,
                () -> userLocationsService.save(new GeocodingAPIResponseDTO(12.123, 12.123, "blabla", "bla", "bla")));

        assertEquals("You already saved this location. Please choose a different location.", exception.getMessage());

        verify(locationRepository, never()).save(location);
        verify(userRepository, never()).save(user);
    }

    @Test
    void removeSavedLocation_Success() {
        user.getLocations().add(location);
        when(sessionService.getCurrentUserSession()).thenReturn(session);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        userLocationsService.removeSavedLocation(location.getId());

        verify(locationRepository).removeLocationByUserId(user.getId(), location.getId());
        verify(userRepository).save(user);

        assertEquals(user.getLocations(), List.of());
    }

    @Test
    void removeSavedLocation_ifNotFound() {
        when(sessionService.getCurrentUserSession()).thenReturn(session);
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundModelException exception = assertThrows(NotFoundModelException.class,
                () -> userLocationsService.removeSavedLocation(location.getId()));
        assertEquals("No such location found.", exception.getMessage());
    }
}

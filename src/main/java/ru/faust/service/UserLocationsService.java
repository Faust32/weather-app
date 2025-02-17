package ru.faust.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.faust.dto.GeocodingAPIResponseDTO;
import ru.faust.exception.AlreadyExistsException;
import ru.faust.exception.NotFoundModelException;
import ru.faust.model.Location;
import ru.faust.model.User;
import ru.faust.repository.LocationRepository;
import ru.faust.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLocationsService {

    private static final Logger logger = LoggerFactory.getLogger(UserLocationsService.class);

    private final UserRepository userRepository;

    private final LocationRepository locationRepository;

    private final SessionService sessionService;

    public List<Location> findAllSaved() {
        Long userId = sessionService.getCurrentUserSession().getUser().getId();
        return locationRepository.findLocationsByUserId(userId);
    }

    @Transactional
    public void save(GeocodingAPIResponseDTO geocodingAPIResponseDTO) {
        User user = sessionService.getCurrentUserSession().getUser();
        logger.info("User {} is attempting to save location: {} ({}, {})",
                user.getUsername(), geocodingAPIResponseDTO.name(), geocodingAPIResponseDTO.lat(), geocodingAPIResponseDTO.lon());

        Location location = locationRepository.findByLongitudeAndLatitude(
                geocodingAPIResponseDTO.lon(),
                geocodingAPIResponseDTO.lat()
        ).orElseGet(() -> {
            Location newLocation = Location.builder()
                    .name(geocodingAPIResponseDTO.name())
                    .usersId(new ArrayList<>())
                    .latitude(geocodingAPIResponseDTO.lat())
                    .longitude(geocodingAPIResponseDTO.lon())
                    .build();
            return locationRepository.save(newLocation);
        });

        boolean exists = userRepository.existsByIdAndLocationsId(user.getId(), location.getId());
        if (exists) {
            logger.warn("User {} attempted to save an already existing location: {} ({}, {})",
                    user.getUsername(), geocodingAPIResponseDTO.name(), geocodingAPIResponseDTO.lat(), geocodingAPIResponseDTO.lon());
            throw new AlreadyExistsException("You already saved this location. Please choose a different location.", "locationService.save");
        }

        location.getUsersId().add(user);
        user.addLocation(location);

        locationRepository.save(location);
        userRepository.save(user);

        logger.info("User {} successfully saved location: {} ({}, {})",
                user.getUsername(), geocodingAPIResponseDTO.name(), geocodingAPIResponseDTO.lat(), geocodingAPIResponseDTO.lon());
    }


    @Transactional
    public void removeSavedLocation(Long locationId) {
        User user = sessionService.getCurrentUserSession().getUser();
        logger.info("User {} is attempting to remove location: {}", user.getUsername(), locationId);
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> {
                    logger.error("No such location: {}", locationId);
                    return new NotFoundModelException("No such location found.", "removeSavedLocation");
                });
        user.getLocations().remove(location);
        locationRepository.removeLocationByUserId(user.getId(), locationId);
        logger.info("User {} successfully removed location: {}", user.getUsername(), locationId);
        userRepository.save(user);

    }

}

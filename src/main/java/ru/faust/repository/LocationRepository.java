package ru.faust.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.faust.model.Location;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    @EntityGraph(attributePaths = "usersId")
    Optional<Location> findByLongitudeAndLatitude(double longitude, double latitude);

    @Query(value = "SELECT l.* FROM locations l JOIN user_location ul ON l.id = ul.location_id WHERE ul.user_id = :userId",
           nativeQuery = true)
    List<Location> findLocationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_location WHERE user_id = :userId AND location_id = :locationId",
            nativeQuery = true)
    void removeLocationByUserId(@Param("userId") Long userId, @Param("locationId") Long locationId);

}
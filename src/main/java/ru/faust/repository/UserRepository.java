package ru.faust.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.faust.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Query("SELECT COUNT(u) > 0 FROM User u JOIN u.locations l WHERE u.id = :userId AND l.id = :locationId")
    boolean existsByIdAndLocationsId(@Param("userId") Long userId, @Param("locationId") Long locationId);


}

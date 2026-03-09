package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.model.location.Location;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByLonAndLat(Double lon, Double lat);
}

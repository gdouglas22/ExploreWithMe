package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.request.Request;

import java.util.Map;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("SELECT r.event.id, COUNT(r) " +
            "FROM Request r " +
            "WHERE r.event.id IN (:eventIds) " +
            "GROUP BY r.event.id")
    Map<Long, Long> countRequestsByEventIds(@Param("eventIds") Set<Long> eventIds);

    @Query("SELECT COUNT(r) " +
            "FROM Request r " +
            "WHERE r.event.id = :eventId")
    Long countRequestsByEventId(@Param("eventId") Long eventId);
}

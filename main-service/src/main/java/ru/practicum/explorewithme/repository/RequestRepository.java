package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.request.Request;

import java.util.List;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query(value = "SELECT r.event_id AS eventId, COUNT(r.id) AS requestCount " +
            "FROM requests r " +
            "WHERE r.event_id IN (:eventIds) " +
            "GROUP BY r.event_id",
            nativeQuery = true)
    List<Object[]> countRequestsByEventIds(@Param("eventIds") Set<Long> eventIds);

    @Query("SELECT COUNT(r) " +
            "FROM Request r " +
            "WHERE r.event.id = :eventId")
    Long countRequestsByEventId(@Param("eventId") Long eventId);

    List<Request> findRequestsByRequesterId(Long id);
}

package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.request.Request;

import java.util.List;
import java.util.Map;

public interface RequestRepository extends JpaRepository<Request, Long> {
    @Query("SELECT r.event.id, COUNT(r) " +
            "FROM Request r " +
            "WHERE r.event.id IN (:eventIds) " +
            "GROUP BY r.event.id")
    Map<Long, Long> countRequestsByEventIds(@Param("eventIds") List<Long> eventIds);

    List<Request> findByEventId(Long eventId);

    @Query("SELECT r " +
            "FROM Request r " +
            "WHERE r.id in :ids")
    List<Request> findByIds(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(r) " +
            "FROM Request r " +
            "WHERE r.event_id = :eventId and r.status = :status")
    Integer countEventRequestsInSpecialStatus(
            @Param("eventId") Long eventId,
            @Param("status") String status
    );
}

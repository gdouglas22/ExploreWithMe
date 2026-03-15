package ru.practicum.explorewithme.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "  AND (:states IS NULL OR e.state IN :states) " +
            "  AND (:categories IS NULL OR e.category.id IN :categories) " +
            "  AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "  AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findByEventAdminRequest(
            @Param("users") List<Long> users,
            @Param("states") List<State> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = ru.practicum.explorewithme.model.event.State.PUBLISHED " +
            "  AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "       OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "  AND (:categories IS NULL OR e.category.id IN :categories) " +
            "  AND (:paid IS NULL OR e.paid = :paid) " +
            "  AND (:rangeStart IS NULL OR e.eventDate >= :rangeStart) " +
            "  AND (:rangeEnd IS NULL OR e.eventDate <= :rangeEnd) " +
            "  AND (:onlyAvailable = FALSE OR e.participantLimit = 0 OR " +
            "       (SELECT COUNT(r) FROM Request r WHERE r.event.id = e.id " +
            "           AND r.status = ru.practicum.explorewithme.model.request.Status.CONFIRMED) < e.participantLimit)")
    List<Event> findPublicEvents(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") boolean onlyAvailable
    );

    Optional<Event> findByIdAndState(Long eventId, State state);
}

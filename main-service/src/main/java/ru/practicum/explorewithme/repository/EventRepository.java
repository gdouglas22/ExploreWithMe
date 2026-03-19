package ru.practicum.explorewithme.repository;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.model.request.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e " +
            "WHERE e.initiator.id IN :users " +
            "AND e.state IN :states " +
            "AND e.category.id IN :categories " +
            "AND e.eventDate >= :rangeStart " +
            "AND e.eventDate <= :rangeEnd")
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

    Optional<Event> findByIdAndState(Long eventId, State state);

    default List<Event> findEventsByFilters(String text, List<Long> categories, Boolean paid,
                                            LocalDateTime start, LocalDateTime end, Boolean onlyAvailable) {
        return findAll((root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get("state"), State.PUBLISHED));

            if (text != null && !text.trim().isEmpty()) {
                String searchPattern = "%" + text.toLowerCase() + "%";
                Predicate annotationMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("annotation")), searchPattern);
                Predicate descriptionMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchPattern);
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.or(annotationMatch, descriptionMatch));
            }

            if (categories != null && !categories.isEmpty()) {
                predicate = criteriaBuilder.and(predicate,
                        root.get("category").get("id").in(categories));
            }

            if (paid != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("paid"), paid));
            }

            if (start != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), start));
            }

            if (end != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), end));
            }

            if (Boolean.TRUE.equals(onlyAvailable)) {
                Subquery<Long> requestCount = query.subquery(Long.class);
                Root<Request> requestRoot = requestCount.from(Request.class);
                requestCount.select(criteriaBuilder.count(requestRoot))
                        .where(
                                criteriaBuilder.equal(requestRoot.get("event"), root),
                                criteriaBuilder.equal(requestRoot.get("status"), Status.CONFIRMED)
                        );

                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.or(
                                criteriaBuilder.isNull(root.get("participantLimit")),
                                criteriaBuilder.equal(root.get("participantLimit"), 0),
                                criteriaBuilder.greaterThan(root.get("participantLimit"), requestCount)
                        )
                );
            }

            return predicate;
        });
    }
}

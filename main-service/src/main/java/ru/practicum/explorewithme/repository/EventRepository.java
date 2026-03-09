package ru.practicum.explorewithme.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.model.event.Event;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);
}

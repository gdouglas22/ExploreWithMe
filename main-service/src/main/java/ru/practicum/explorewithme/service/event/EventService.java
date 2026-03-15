package ru.practicum.explorewithme.service.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.dto.event.EventAdminRequest;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;

public interface EventService {
    Page<EventFullDto> getEventByParam(EventAdminRequest eventAdminRequest, Pageable pageable);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    boolean eventExists(Long eventId);
}

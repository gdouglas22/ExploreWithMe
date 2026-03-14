package ru.practicum.explorewithme.service.event;

import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.model.event.Event;

import java.util.List;

public interface EventService {
    EventFullDto create(Long userid, NewEventDto newEventDto);

    EventFullDto getByUserIdAndId(Long userId, Long id);

    List<EventFullDto> getByUserId(Long userId, int from, int size);

    EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventData);

    Event getEntityById(Long id);
}

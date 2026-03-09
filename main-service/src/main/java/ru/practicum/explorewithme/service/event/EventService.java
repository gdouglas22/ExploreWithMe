package ru.practicum.explorewithme.service.event;

import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;

import java.util.List;

public interface EventService {
    EventFullDto create(Long userid, NewEventDto newEventDto);

    EventFullDto getByUserIdAndId(Long userId, Long id);

    List<EventFullDto> getByUserId(Long userId, int from, int size);
}

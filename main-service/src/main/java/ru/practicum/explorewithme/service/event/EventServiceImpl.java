package ru.practicum.explorewithme.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.dto.location.LocationDto;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.model.category.Category;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;
import ru.practicum.explorewithme.model.location.Location;
import ru.practicum.explorewithme.model.user.User;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.LocationRepository;
import ru.practicum.explorewithme.service.category.CategoryService;
import ru.practicum.explorewithme.service.user.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    private final Clock clock;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User initiator = userService.getEntityById(userId);
        Category category = categoryService.getEntityById(newEventDto.getCategory());
        Event event = EventMapper.mapToNewEvent(newEventDto);
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now(clock));
        Location location = findOrCreateLocation(newEventDto.getLocation());
        event.setLocation(location);
        event = eventRepository.save(event);

        return EventMapper.mapToFullDto(event);
    }

    public EventFullDto getByUserIdAndId(Long userId, Long id) {
        // проверка на суествование пользователя по его id
        userService.getEntityById(userId);

        return eventRepository.findById(id)
                .map(EventMapper::mapToFullDto)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d не найдено", id)));
    }

    public List<EventFullDto> getByUserId(Long userId, int from, int size) {
        User user = userService.getEntityById(userId);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return eventRepository.findAllByInitiatorId(user.getId(), page).stream()
                .map(EventMapper::mapToFullDto)
                .toList();
    }

    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEventData) {
        User user = userService.getEntityById(userId);
        Event currentEvent = getEntityById(eventId);
        if (!(currentEvent.getState().equals(State.CANCELED) || currentEvent.getState().equals(State.PENDING))) {
            throw new ConflictDataException("Можно редактировать только отмененные или в состоянии модерации события.");
        }
        if (!currentEvent.getInitiator().getId().equals(user.getId())) {
            throw new ConflictDataException(
                    String.format("Пользователь с id = %d не является автором события с id = %d", user.getId(), currentEvent.getId())
            );
        }
        if (updateEventData.hasCategory()) {
            Category updatedCategory = categoryService.getEntityById(updateEventData.getCategory());
            currentEvent.setCategory(updatedCategory);
        }
        EventMapper.updateEventData(currentEvent, updateEventData);
        currentEvent = eventRepository.save(currentEvent);
        return EventMapper.mapToFullDto(currentEvent);
    }

    public Event getEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d не найдено", id)));
    }

    private Location findOrCreateLocation(LocationDto locationDto) {
        return locationRepository.findByLonAndLat(locationDto.getLon(), locationDto.getLat())
                .orElseGet(() -> {
                    Location newLocation = new Location();
                    newLocation.setLon(locationDto.getLon());
                    newLocation.setLat(locationDto.getLat());
                    return locationRepository.save(newLocation);
                });
    }
}

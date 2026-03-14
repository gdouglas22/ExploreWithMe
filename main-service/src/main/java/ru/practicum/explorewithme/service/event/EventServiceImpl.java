package ru.practicum.explorewithme.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.dto.ViewStats;
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
import ru.practicum.explorewithme.service.request.RequestService;
import ru.practicum.explorewithme.service.user.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;

    private final StatClient statClient;
    private final Clock clock;

    private final String uri = "/events/";

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

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id = %d не найдено", id)));

        Long eventStats = getNotUniqueStatsByEventId(event.getId(), event.getCreatedOn());
        Long confirmedRequests = requestService.countRequestsByEventId(event.getId());

        EventFullDto response = EventMapper.mapToFullDto(event);
        response.setConfirmedRequests(confirmedRequests.intValue());
        response.setViews(eventStats.intValue());

        return response;
    }

    public List<EventFullDto> getByUserId(Long userId, int from, int size) {
        User user = userService.getEntityById(userId);
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);

        List<Event> events = eventRepository.findAllByInitiatorId(user.getId(), page);

        LocalDateTime earliestDatetime = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        Set<Long> eventsIds = events.stream().map(Event::getId).collect(Collectors.toSet());
        Map<Long, Long> eventsStats = getNotUniqueStatsByEventIds(eventsIds, earliestDatetime);
        Map<Long, Long> confirmedEventsRequests = requestService.countRequestsByEventIds(eventsIds);

        Map<Long, Event> eventsMap = events.stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        return events.stream()
                .map(EventMapper::mapToFullDto)
                .peek(eventFullDto -> {
                    eventFullDto.setConfirmedRequests(confirmedEventsRequests.get(eventFullDto.getId()).intValue());
                    eventFullDto.setViews(eventsStats.get(eventFullDto.getId()).intValue());
                })
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

    private Map<Long, Long> getNotUniqueStatsByEventIds(Set<Long> eventIds, LocalDateTime from) {
        List<String> uris = new ArrayList<>();
        eventIds.forEach(eventId -> uris.add(uri + eventId));

        List<ViewStats> viewStats = statClient.getStat(from, LocalDateTime.now(), uris, false);
        return viewStats.stream()
                .collect(Collectors.toMap(
                        viewStat -> {
                            String[] parts = viewStat.uri().split("/");
                            String numberStr = parts[parts.length - 1];
                            return Long.parseLong(numberStr);
                        },
                        ViewStats::hits,
                        (existing, replacement) -> existing));
    }

    private Long getNotUniqueStatsByEventId(Long eventId, LocalDateTime from) {
        List<String> uris = List.of(uri + eventId);

        List<ViewStats> viewStats = statClient.getStat(from, LocalDateTime.now(), uris, false);
        return viewStats.stream()
                .map(ViewStats::hits)
                .mapToLong(Long::longValue)
                .sum();

    }
}

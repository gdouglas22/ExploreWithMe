package ru.practicum.explorewithme.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.dto.ViewStats;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.event.EventAdminRequest;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.model.category.Category;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;
import ru.practicum.explorewithme.model.event.StateAction;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.service.category.CategoryService;
import ru.practicum.explorewithme.service.request.RequestService;
import ru.practicum.explorewithme.model.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestService requestService;
    private final CategoryService categoryService;
    private final StatClient statClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String uri = "/events/";

    @Override
    public Page<EventFullDto> getEventByParam(EventAdminRequest eventAdminRequest, Pageable pageable) {
        log.info("Try to get event by param={}", eventAdminRequest);

        List<State> states = parseState(eventAdminRequest.states());
        LocalDateTime rangeStart = parseDate(eventAdminRequest.rangeStart());
        LocalDateTime rangeEnd = parseDate(eventAdminRequest.rangeEnd());

        Page<Event> page = eventRepository.findByEventAdminRequest(
                eventAdminRequest.users(),
                states,
                eventAdminRequest.categories(),
                rangeStart,
                rangeEnd,
                pageable);

        LocalDateTime earliestDate = getEarliestDateInPage(page);

        Set<Long> eventIds = getEventId(page);

        Map<Long, Long> amountRequestsByEventIds = requestService.countRequestsByEventIds(eventIds);
        Map<Long, Long> viewByEventIds = getNotUniqueStatsByEventIds(eventIds, earliestDate);

        log.info("Return event");

        return page.map(event -> {
            Long amountRequest = amountRequestsByEventIds.get(event.getId());
            Long amountRequestResult = amountRequest == null ? 0 : amountRequest;

            Long stat = viewByEventIds.get(event.getId());
            Long resultStat = stat == null ? 0 : stat;

            return EventMapper.toEventFullDto(event, amountRequestResult, resultStat);
        });
    }

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        log.info("Try to create event by userId={}", userId);
        User initiator = getUserById(userId);
        CategoryDto categoryDto = categoryService.getCateGoryById(newEventDto.getCategory());

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(new Category(categoryDto.id(), categoryDto.name()))
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(parseDate(newEventDto.getEventDate()))
                .initiator(initiator)
                .location(LocationMapper.mapToLocation(newEventDto.getLocation()))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .publishedOn(null)
                .requestModeration(newEventDto.getRequestModeration())
                .state(State.PENDING)
                .title(newEventDto.getTitle())
                .build();

        Event savedEvent = eventRepository.save(event);
        return EventMapper.toEventFullDto(savedEvent, 0L, 0L);
    }

    @Override
    public EventFullDto getByUserIdAndId(Long userId, Long eventId) {
        Event event = getEventByUserIdAndIdOrThrow(userId, eventId);
        Long confirmedRequests = requestService.countRequestsByEventId(eventId);
        Long views = getNotUniqueStatsByEventId(eventId, event.getCreatedOn());
        return EventMapper.toEventFullDto(event, confirmedRequests, views);
    }

    @Override
    public List<EventShortDto> getByUserId(Long userId, int from, int size) {
        getUserById(userId);
        PageRequest pageRequest = PageRequest.of(from > 0 ? from / size : 0, size);
        Page<Event> page = eventRepository.findAllByInitiatorId(userId, pageRequest);
        if (page.isEmpty()) {
            return List.of();
        }

        LocalDateTime earliestDate = getEarliestDateInPage(page);
        Set<Long> eventIds = getEventId(page);
        Map<Long, Long> amountRequestsByEventIds = requestService.countRequestsByEventIds(eventIds);
        Map<Long, Long> viewByEventIds = getNotUniqueStatsByEventIds(eventIds, earliestDate);

        return page.getContent().stream()
                .map(event -> EventMapper.toEventShortDto(
                        event,
                        amountRequestsByEventIds.getOrDefault(event.getId(), 0L),
                        viewByEventIds.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto update(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getEventByUserIdAndIdOrThrow(userId, eventId);

        if (!(event.getState().equals(State.PENDING) || event.getState().equals(State.CANCELED))) {
            throw new ConflictDataException("Only pending or canceled events can be changed");
        }

        if (updateEvent.hasAnnotation()) {
            event.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.hasCategory()) {
            CategoryDto categoryDto = categoryService.getCateGoryById(updateEvent.getCategory());
            event.setCategory(new Category(categoryDto.id(), categoryDto.name()));
        }
        if (updateEvent.hasDescription()) {
            event.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.hasEventDate()) {
            event.setEventDate(parseDate(updateEvent.getEventDate()));
        }
        if (updateEvent.hasLocation()) {
            event.setLocation(LocationMapper.mapToLocation(updateEvent.getLocation()));
        }
        if (updateEvent.hasPaid()) {
            event.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.hasParticipantLimit()) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.hasRequestModeration()) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.hasTitle()) {
            event.setTitle(updateEvent.getTitle());
        }
        if (updateEvent.hasStateAction()) {
            if (updateEvent.getStateAction().equals(StateAction.SEND_TO_REVIEW)) {
                event.setState(State.PENDING);
            } else if (updateEvent.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
                event.setState(State.CANCELED);
            }
        }

        Event savedEvent = eventRepository.save(event);
        Long confirmedRequests = requestService.countRequestsByEventId(savedEvent.getId());
        Long views = getNotUniqueStatsByEventId(savedEvent.getId(), savedEvent.getCreatedOn());
        return EventMapper.toEventFullDto(savedEvent, confirmedRequests, views);
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (optionalEvent.isEmpty()) {
            log.error("Event not found by ID={}", eventId);
            throw new NotFoundException("Event not found by ID=" + eventId);
        }
        Event event = optionalEvent.get();
        Event updatedEvent = validateAndUpdate(event, updateEventAdminRequest);
        Event savedEvent = eventRepository.save(updatedEvent);

        Long amountRequestsByEventId = requestService.countRequestsByEventId(savedEvent.getId());
        Long viewByEventId = getNotUniqueStatsByEventId(savedEvent.getId(), savedEvent.getCreatedOn());

        return EventMapper.toEventFullDto(event, amountRequestsByEventId, viewByEventId);
    }

    @Override
    public boolean eventExists(Long eventId) {
        return eventRepository.existsById(eventId);
    }

    private Set<Long> getEventId(Page<Event> page) {
        return page.getContent()
                .stream()
                .map(Event::getId)
                .collect(Collectors.toSet());
    }

    private LocalDateTime getEarliestDateInPage(Page<Event> page) {
        return page.getContent()
                .stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    private LocalDateTime parseDate(String stringDate) {
        try {
            return LocalDateTime.parse(stringDate, formatter);
        } catch (DateTimeParseException exception) {
            log.error("Not valid value stringDate={}", stringDate);
            throw new BadRequestException("Not valid value stringDate" + stringDate);
        }
    }

    private List<State> parseState(List<String> states) {
        try {
            return states.stream()
                    .map(State::valueOf)
                    .toList();
        } catch (IllegalArgumentException e) {
            log.error("Not valid value states={}", states);
            throw new BadRequestException("Not valid value states=" + states);
        }
    }

    private Map<Long, Long> getNotUniqueStatsByEventIds(Set<Long> eventIds, LocalDateTime from) {
        if (eventIds.isEmpty() || from == null) {
            return Map.of();
        }
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
        if (from == null) {
            return 0L;
        }
        List<String> uris = List.of(uri + eventId);

        List<ViewStats> viewStats = statClient.getStat(from, LocalDateTime.now(), uris, false);
        return viewStats.stream()
                .map(ViewStats::hits)
                .mapToLong(Long::longValue)
                .sum();

    }

    private Event validateAndUpdate(Event event, UpdateEventAdminRequest updateEventAdminRequest) {

        if (updateEventAdminRequest.hasStateAction()) {
            StateAction action = StateAction.valueOf(updateEventAdminRequest.getStateAction());
            if (!event.getState().equals(State.PENDING)) {
                log.info("Cannot publish the event because it's not in the right state={}", event.getState());
                throw new ConflictDataException("Cannot publish the event because it's not in the right state");
            }
            if (action.equals(StateAction.PUBLISH_EVENT)) {
                event.setState(State.PUBLISHED);
            } else {
                event.setState(State.CANCELED);
            }
        }

        if (updateEventAdminRequest.hasEventDate()) {
            LocalDateTime eventDate = parseDate(updateEventAdminRequest.getEventDate());
            if (eventDate.isBefore(event.getPublishedOn().plusHours(1))) {
                log.error("event date can't be earlier than={}", event.getPublishedOn().plusHours(1));
                throw new ConflictDataException("event date can't be earlier than="
                        + event.getPublishedOn().plusHours(1));
            }
            event.setEventDate(eventDate);
        }

        if (updateEventAdminRequest.hasAnnotation()) {
            event.setAnnotation(updateEventAdminRequest.getAnnotation());
        }

        if (updateEventAdminRequest.hasCategory()) {
            CategoryDto categoryDto = categoryService.getCateGoryById(updateEventAdminRequest.getCategory());
            event.setCategory(new Category(categoryDto.id(), categoryDto.name()));
        }

        if (updateEventAdminRequest.hasDescription()) {
            event.setDescription(updateEventAdminRequest.getDescription());
        }

        if (updateEventAdminRequest.hasLocation()) {
            event.setLocation(updateEventAdminRequest.getLocation());
        }

        if (updateEventAdminRequest.hasPaid()) {
            event.setPaid(updateEventAdminRequest.getPaid());
        }

        if (updateEventAdminRequest.hasParticipantLimit()) {
            event.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }

        if (updateEventAdminRequest.hasRequestModeration()) {
            event.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }

        if (updateEventAdminRequest.hasTitle()) {
            event.setTitle(updateEventAdminRequest.getTitle());
        }

        return event;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found by ID=" + userId));
    }

    private Event getEventByUserIdAndIdOrThrow(Long userId, Long eventId) {
        getUserById(userId);
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found by ID=" + eventId));
    }
}

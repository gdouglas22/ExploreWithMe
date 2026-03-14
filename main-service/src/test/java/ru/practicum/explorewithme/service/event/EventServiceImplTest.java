package ru.practicum.explorewithme.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.dto.ViewStats;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.dto.location.LocationDto;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private RequestService requestService;

    @Mock
    private StatClient statClient;

    private Clock fixedClock;
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
                Instant.parse("2024-01-01T10:00:00Z"),
                ZoneId.systemDefault()
        );
        eventService = new EventServiceImpl(
                eventRepository,
                locationRepository,
                userService,
                categoryService,
                requestService,
                statClient,
                fixedClock
        );
    }

    @Test
    void create_shouldCreateEventWithExistingLocation() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        Category category = Category.builder().id(2L).build();
        LocationDto locationDto = new LocationDto(10.0, 20.0);
        Location location = Location.builder().id(3L).lat(20.0).lon(10.0).build();

        NewEventDto dto = NewEventDto.builder()
                .annotation("A".repeat(20))
                .category(category.getId())
                .description("D".repeat(20))
                .eventDate("2024-01-02 12:00:00")
                .location(locationDto)
                .title("Title".repeat(3))
                .build();

        Event savedEvent = Event.builder()
                .id(5L)
                .annotation(dto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now(fixedClock))
                .description(dto.getDescription())
                .eventDate(LocalDateTime.of(2024, 1, 2, 12, 0))
                .initiator(user)
                .location(location)
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(State.PENDING)
                .title(dto.getTitle())
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(categoryService.getEntityById(dto.getCategory())).thenReturn(category);
        when(locationRepository.findByLonAndLat(locationDto.getLon(), locationDto.getLat()))
                .thenReturn(Optional.of(location));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventFullDto result = eventService.create(userId, dto);

        assertNotNull(result);
        assertEquals(savedEvent.getId(), result.getId());
        verify(eventRepository).save(any(Event.class));
        verify(locationRepository).findByLonAndLat(locationDto.getLon(), locationDto.getLat());
    }

    @Test
    void create_shouldCreateEventWithNewLocationWhenNotFound() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        Category category = Category.builder().id(2L).build();
        LocationDto locationDto = new LocationDto(10.0, 20.0);
        Location newLocation = Location.builder().id(3L).lat(20.0).lon(10.0).build();

        NewEventDto dto = NewEventDto.builder()
                .annotation("A".repeat(20))
                .category(category.getId())
                .description("D".repeat(20))
                .eventDate("2024-01-02 12:00:00")
                .location(locationDto)
                .title("Title".repeat(3))
                .build();

        Event savedEvent = Event.builder()
                .id(5L)
                .annotation(dto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now(fixedClock))
                .description(dto.getDescription())
                .eventDate(LocalDateTime.of(2024, 1, 2, 12, 0))
                .initiator(user)
                .location(newLocation)
                .paid(dto.getPaid())
                .participantLimit(dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration())
                .state(State.PENDING)
                .title(dto.getTitle())
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(categoryService.getEntityById(dto.getCategory())).thenReturn(category);
        when(locationRepository.findByLonAndLat(locationDto.getLon(), locationDto.getLat()))
                .thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenReturn(newLocation);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        EventFullDto result = eventService.create(userId, dto);

        assertNotNull(result);
        assertEquals(savedEvent.getId(), result.getId());
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void getByUserIdAndId_shouldReturnEventWithViewsAndConfirmedRequests_whenExists() {
        Long userId = 1L;
        Long eventId = 2L;
        User user = User.builder().id(userId).build();
        LocalDateTime createdOn = LocalDateTime.now(fixedClock);
        Event event = Event.builder()
                .id(eventId)
                .initiator(user)
                .category(Category.builder().id(3L).build())
                .location(Location.builder().id(4L).lat(10.0).lon(20.0).build())
                .state(State.PENDING)
                .createdOn(createdOn)
                .eventDate(LocalDateTime.now(fixedClock).plusDays(1))
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(requestService.countRequestsByEventId(eventId)).thenReturn(5L);
        when(statClient.getStat(any(LocalDateTime.class), any(LocalDateTime.class), any(List.class), eq(false)))
                .thenReturn(List.of(new ViewStats("ewm", "/events/2", 10L)));

        EventFullDto result = eventService.getByUserIdAndId(userId, eventId);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals(5, result.getConfirmedRequests());
        assertEquals(10, result.getViews());
        verify(userService).getEntityById(userId);
        verify(eventRepository).findById(eventId);
        verify(requestService).countRequestsByEventId(eventId);
        verify(statClient).getStat(any(LocalDateTime.class), any(LocalDateTime.class), any(List.class), eq(false));
    }

    @Test
    void getByUserIdAndId_shouldThrowNotFound_whenEventMissing() {
        Long userId = 1L;
        Long eventId = 2L;

        when(userService.getEntityById(userId)).thenReturn(User.builder().id(userId).build());
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getByUserIdAndId(userId, eventId));
    }

    @Test
    void getByUserId_shouldReturnEventsWithViewsAndConfirmedRequests() {
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        Long eventId = 2L;
        Event event = Event.builder()
                .id(eventId)
                .initiator(user)
                .category(Category.builder().id(3L).build())
                .location(Location.builder().id(4L).lat(10.0).lon(20.0).build())
                .state(State.PENDING)
                .createdOn(LocalDateTime.now(fixedClock))
                .eventDate(LocalDateTime.now(fixedClock).plusDays(1))
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(eventRepository.findAllByInitiatorId(eq(userId), any(PageRequest.class)))
                .thenReturn(List.of(event));
        when(requestService.countRequestsByEventIds(Set.of(eventId)))
                .thenReturn(Map.of(eventId, 3L));
        when(statClient.getStat(any(LocalDateTime.class), any(LocalDateTime.class), any(List.class), eq(false)))
                .thenReturn(List.of(new ViewStats("ewm", "/events/2", 7L)));

        List<EventFullDto> result = eventService.getByUserId(userId, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(event.getId(), result.getFirst().getId());
        assertEquals(3, result.getFirst().getConfirmedRequests());
        assertEquals(7, result.getFirst().getViews());
        verify(requestService).countRequestsByEventIds(Set.of(eventId));
        verify(statClient).getStat(any(LocalDateTime.class), any(LocalDateTime.class), any(List.class), eq(false));
    }

    @Test
    void update_shouldUpdateEvent_whenStateAllowedAndInitiatorMatches() {
        Long userId = 1L;
        Long eventId = 2L;
        User user = User.builder().id(userId).build();
        Category category = Category.builder().id(3L).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(user)
                .category(category)
                .location(Location.builder().id(4L).lat(10.0).lon(20.0).build())
                .state(State.PENDING)
                .createdOn(LocalDateTime.now(fixedClock))
                .eventDate(LocalDateTime.now(fixedClock).plusDays(1))
                .title("Old title")
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("New title")
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventFullDto result = eventService.update(userId, eventId, updateRequest);

        assertNotNull(result);
        assertEquals("New title", result.getTitle());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void update_shouldThrowConflict_whenStateNotAllowed() {
        Long userId = 1L;
        Long eventId = 2L;
        User user = User.builder().id(userId).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(user)
                .category(Category.builder().id(3L).build())
                .location(Location.builder().id(4L).lat(10.0).lon(20.0).build())
                .state(State.PUBLISHED)
                .createdOn(LocalDateTime.now(fixedClock))
                .eventDate(LocalDateTime.now(fixedClock).plusDays(1))
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("New title")
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictDataException.class, () -> eventService.update(userId, eventId, updateRequest));
    }

    @Test
    void update_shouldThrowConflict_whenUserNotInitiator() {
        Long userId = 1L;
        Long eventId = 2L;
        User user = User.builder().id(userId).build();
        User anotherUser = User.builder().id(999L).build();
        Event event = Event.builder()
                .id(eventId)
                .initiator(anotherUser)
                .category(Category.builder().id(3L).build())
                .location(Location.builder().id(4L).lat(10.0).lon(20.0).build())
                .state(State.PENDING)
                .createdOn(LocalDateTime.now(fixedClock))
                .eventDate(LocalDateTime.now(fixedClock).plusDays(1))
                .build();

        UpdateEventUserRequest updateRequest = UpdateEventUserRequest.builder()
                .title("New title")
                .build();

        when(userService.getEntityById(userId)).thenReturn(user);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThrows(ConflictDataException.class, () -> eventService.update(userId, eventId, updateRequest));
    }

    @Test
    void getEntityById_shouldReturnEvent_whenExists() {
        Long eventId = 2L;
        Event event = Event.builder().id(eventId).build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        Event result = eventService.getEntityById(eventId);

        assertNotNull(result);
        assertEquals(eventId, result.getId());
    }

    @Test
    void getEntityById_shouldThrowNotFound_whenMissing() {
        Long eventId = 2L;
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getEntityById(eventId));
    }
}

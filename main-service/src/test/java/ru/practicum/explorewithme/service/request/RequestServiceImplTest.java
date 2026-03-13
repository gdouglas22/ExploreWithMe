package ru.practicum.explorewithme.service.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.model.category.Category;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;
import ru.practicum.explorewithme.model.location.Location;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.model.request.Status;
import ru.practicum.explorewithme.model.user.User;
import ru.practicum.explorewithme.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestServiceImplTest {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    RequestService requestService;

    private Event savedEvent;
    private Event savedEvent2;
    private Event savedEvent3;
    private User savedRequester;

    @BeforeEach
    void setUp() {
        User user = new User(1L, "user", "email");
        User savedUser = userRepository.save(user);

        Category category = new Category(1L, "category");
        Category savedCategory = categoryRepository.save(category);

        Location location = new Location(1L, 123f, 123f);
        Location savedLocation = locationRepository.save(location);

        Event event = new Event(1L, "annotation", savedCategory, LocalDateTime.now().minusHours(2),
                "description", LocalDateTime.now(), savedUser, savedLocation, true, 10,
                LocalDateTime.now().minusHours(1), true, State.PENDING, "title");
        savedEvent = eventRepository.save(event);

        Event event2 = new Event(2L, "annotation", savedCategory, LocalDateTime.now().minusHours(2),
                "description", LocalDateTime.now(), savedUser, savedLocation, true, 10,
                LocalDateTime.now().minusHours(1), true, State.PENDING, "title");
        savedEvent2 = eventRepository.save(event2);

        Event event3 = new Event(3L, "annotation", savedCategory, LocalDateTime.now().minusHours(2),
                "description", LocalDateTime.now(), savedUser, savedLocation, true, 10,
                LocalDateTime.now().minusHours(1), true, State.PENDING, "title");
        savedEvent3 = eventRepository.save(event3);

        User requester = new User(2L, "requester", "email");
        savedRequester = userRepository.save(requester);

        Request firstRequest = new Request(1L, LocalDateTime.now(), savedEvent, savedRequester, Status.PENDING);
        requestRepository.save(firstRequest);

        Request secondRequest = new Request(2L, LocalDateTime.now().plusMinutes(1), savedEvent2, savedRequester, Status.PENDING);
        requestRepository.save(secondRequest);

        Request thirdRequest = new Request(3L, LocalDateTime.now().plusMinutes(2), savedEvent3, savedRequester, Status.PENDING);
        requestRepository.save(thirdRequest);
    }

    @Test
    void countRequestsByEventIdsShouldContCorrectly() {
        Map<Long, Long> mapEventIdAmountRequest = requestService.countRequestsByEventIds(Set.of(savedEvent.getId()));

        assertEquals(1L, mapEventIdAmountRequest.get(savedEvent.getId()));
    }

    @Test
    void countRequestsByEventIdsShouldReturnEmptyMapWhenNoRequestByEventIds() {
        Map<Long, Long> mapEventIdAmountRequest = requestService.countRequestsByEventIds(Set.of(1000L));

        assertTrue(mapEventIdAmountRequest.isEmpty());
    }

    @Test
    void getUserRequestsShouldReturnListCorrectly() {
        List<ParticipationRequestDto> expectedList = requestService.getUserRequests(savedRequester.getId());

        assertEquals(3, expectedList.size());
        assertEquals(savedEvent.getId(), expectedList.getFirst().event());
        assertEquals(savedEvent2.getId(), expectedList.get(1).event());
        assertEquals(savedEvent3.getId(), expectedList.getLast().event());
    }

    @Test
    void getUserRequestsShouldReturnEmptyListWhenNoRequestFromRequestor() {
        List<ParticipationRequestDto> expectedList = requestService.getUserRequests(1000L);

        assertTrue(expectedList.isEmpty());
    }

    @Test
    void countRequestsByEventIdShouldContCorrectly() {
        Long amountRequests = requestService.countRequestsByEventId(savedEvent.getId());

        assertEquals(1L, amountRequests);
    }

    @Test
    void countRequestsByEventIdShouldReturnZeroWhenNotRequestNyEventId() {
        Long amountRequests = requestService.countRequestsByEventId(1000L);

        System.out.println("amountRequests=" + amountRequests);

        assertEquals(0L, amountRequests);
    }
}
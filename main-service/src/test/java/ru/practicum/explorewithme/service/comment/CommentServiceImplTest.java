package ru.practicum.explorewithme.service.comment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewComment;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.model.category.Category;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;
import ru.practicum.explorewithme.model.location.Location;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.model.request.Status;
import ru.practicum.explorewithme.model.user.User;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.LocationRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.request.RequestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceImplTest {
    private final DateTimeFormatter customFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    private User eventVisitor;

    private User eventInitiator;

    private Event savedEvent;

    private Event savedFutureEvent;

    @BeforeEach
    void createDataInDB() {
        Category category = Category.builder()
                .id(1L)
                .name("category")
                .build();
        Category savedCategory = categoryRepository.save(category);

        User user1 = User.builder()
                .id(1L)
                .name("initiator_user")
                .email("initiator_user@test.ru")
                .build();
        eventInitiator = userRepository.save(user1);

        User user2 = User.builder()
                .id(2L)
                .name("requestor_user")
                .email("requestor_user@test.ru")
                .build();
        eventVisitor = userRepository.save(user2);

        Location location = Location.builder()
                .id(1L)
                .lat(1.1f)
                .lon(2.2f)
                .build();
        Location savedLocation = locationRepository.save(location);

        Event event1 = Event.builder()
                .id(1L)
                .annotation("first_annotation")
                .category(savedCategory)
                .createdOn(LocalDateTime.now().minusHours(5))
                .description("first_description")
                .eventDate(LocalDateTime.now().minusHours(3))
                .initiator(eventInitiator)
                .location(savedLocation)
                .paid(true)
                .participantLimit(10)
                .publishedOn(LocalDateTime.now().minusHours(4))
                .requestModeration(true)
                .state(State.PENDING)
                .title("test_title")
                .build();
        savedEvent = eventRepository.save(event1);

        Event event2 = Event.builder()
                .id(1L)
                .annotation("first_annotation")
                .category(savedCategory)
                .createdOn(LocalDateTime.now().minusHours(5))
                .description("first_description")
                .eventDate(LocalDateTime.now().plusHours(2))
                .initiator(eventInitiator)
                .location(savedLocation)
                .paid(true)
                .participantLimit(10)
                .publishedOn(LocalDateTime.now().minusHours(4))
                .requestModeration(true)
                .state(State.PENDING)
                .title("test_title")
                .build();
        savedFutureEvent = eventRepository.save(event2);
    }

    @Test
    void createCommentShouldCreateCommentCorrectly() throws Exception {
        Request request = Request.builder()
                .created(savedEvent.getPublishedOn().plusMinutes(5))
                .event(savedEvent)
                .requester(eventVisitor)
                .status(Status.CONFIRMED)
                .build();
        requestRepository.save(request);

        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);
        CommentDto resultComment = commentService.createComment(eventVisitor.getId(), savedEvent.getId(), newComment);

        Assertions.assertNotNull(resultComment.getId());
        Assertions.assertEquals(commentText, resultComment.getText());
        Assertions.assertEquals(savedEvent.getId(), resultComment.getEvent());
        Assertions.assertEquals(eventVisitor.getId(), resultComment.getUser());
        Assertions.assertTrue(LocalDateTime.parse(resultComment.getCreatedOn(), customFormatter)
                .isAfter(LocalDateTime.now().minusHours(1)));
    }

    @Test
    void createCommentShouldThrowNotFoundWhenNoUserInDB() throws Exception {
        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> commentService.createComment(1000L, savedEvent.getId(), newComment));
        Assertions.assertTrue(exception.getMessage().contains("Couldn't find user by id=1000"));
    }

    @Test
    void createCommentShouldThrowNotFoundWhenNoEventInDB() throws Exception {
        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> commentService.createComment(eventVisitor.getId(), 1000L, newComment));
        Assertions.assertTrue(exception.getMessage().contains("Couldn't find event by id=1000"));
    }

    @Test
    void createCommentShouldThrowConflictWhenEventOwnerCreateComment() throws Exception {
        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);

        ConflictDataException exception = Assertions.assertThrows(ConflictDataException.class,
                () -> commentService.createComment(eventInitiator.getId(), savedEvent.getId(), newComment));
        Assertions.assertTrue(exception.getMessage().contains("User cant create comment for his event"));
    }

    @Test
    void createCommentShouldThrowNotFoundWhenNoRequestFromEventVisitor() throws Exception {
        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);

        NotFoundException exception = Assertions.assertThrows(NotFoundException.class,
                () -> commentService.createComment(eventVisitor.getId(), savedEvent.getId(), newComment));
        Assertions.assertTrue(exception.getMessage().contains(("Couldn't find request from requestorId="
                + eventVisitor.getId() + " to eventId=" + savedEvent.getId())));
    }

    @Test
    void createCommentShouldThrowConflictWhenRequestNotConfirmed() throws Exception {
        Request request = Request.builder()
                .created(savedEvent.getPublishedOn().plusMinutes(5))
                .event(savedEvent)
                .requester(eventVisitor)
                .status(Status.REJECTED)
                .build();
        requestRepository.save(request);

        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);

        ConflictDataException exception = Assertions.assertThrows(ConflictDataException.class,
                () -> commentService.createComment(eventVisitor.getId(), savedEvent.getId(), newComment));
        Assertions.assertTrue(exception.getMessage().contains(("Create comment can only event's visitor")));
    }

    @Test
    void createCommentShouldThrowConflictWhenCommentBeforeEventDate() throws Exception {
        Request request = Request.builder()
                .created(savedFutureEvent.getPublishedOn().plusMinutes(5))
                .event(savedFutureEvent)
                .requester(eventVisitor)
                .status(Status.CONFIRMED)
                .build();
        requestRepository.save(request);

        String commentText = "test comment".repeat(20);
        NewComment newComment = new NewComment(commentText);

        ConflictDataException exception = Assertions.assertThrows(ConflictDataException.class,
                () -> commentService.createComment(eventVisitor.getId(), savedFutureEvent.getId(), newComment));
        Assertions.assertTrue(exception.getMessage().contains(("Comments can be left only after the event begins")));
    }
}
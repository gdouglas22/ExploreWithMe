package ru.practicum.explorewithme.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.NewComment;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.model.comment.Comment;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.model.request.Status;
import ru.practicum.explorewithme.model.user.User;
import ru.practicum.explorewithme.repository.CommentRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.request.RequestRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewComment newComment) {
        log.info("Try to create comment userId={}, eventId={}, newComment={}", userId, eventId, newComment);
        User user = getUserFromDB(userId);
        Event event = getEventFromDB(eventId);
        checkUserNotEventOwner(event, user);
        Request request = getRequestFromDB(event, user);
        checkRequestWasConfirmed(request);
        checkEventWasStarted(event);
        Comment comment = CommentMapper.toComment(newComment, user, event);
        Comment saveComment = commentRepository.save(comment);
        log.info("Comment was saved");
        return CommentMapper.toDto(saveComment);
    }

    private void checkEventWasStarted(Event event) {
        if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new ConflictDataException("Comments can be left only after the event begins");
        }
    }

    private void checkRequestWasConfirmed(Request request) {
        if (!request.getStatus().equals(Status.CONFIRMED)) {
            throw new ConflictDataException("Create comment can only event's visitor");
        }
    }

    private Request getRequestFromDB(Event event, User user) {
        Optional<Request> requestOptional = requestRepository.findByRequesterIdAndEventId(user.getId(), event.getId());
        return requestOptional.orElseThrow(
                () -> new NotFoundException("Couldn't find request from requestorId="
                        + user.getId() + " to eventId=" + event.getId()));
    }

    private void checkUserNotEventOwner(Event event, User user) {
        if (event.getInitiator().getId().equals(user.getId())) {
            throw new ConflictDataException("User cant create comment for his event");
        }
    }

    private Event getEventFromDB(Long eventId) {
        Optional<Event> eventOptional = eventRepository.findById(eventId);
        return eventOptional.orElseThrow(() -> new NotFoundException("Couldn't find event by id=" + eventId));
    }

    private User getUserFromDB(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.orElseThrow(() -> new NotFoundException("Couldn't find user by id=" + userId));
    }
}

package ru.practicum.explorewithme.service.request;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.event.State;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.model.request.Status;
import ru.practicum.explorewithme.model.user.User;
import ru.practicum.explorewithme.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RequestRepository requestRepository;

    @Override
    public Map<Long, Long> countRequestsByEventIds(Set<Long> eventIds) {
        log.info("Try to count request by event ids={}", eventIds);
        if (eventIds == null) {
            log.error("Try to get requests count by EventIds=null");
            throw new BadRequestException("Try to get requests count by EventIds=null");
        }
        if (eventIds.isEmpty()) {
            return new HashMap<>();
        }
        return getRequestsByEventIds(eventIds);
    }

    @Override
    public Long countRequestsByEventId(Long eventId) {
        log.info("Try to count request by event id={}", eventId);
        if (eventId == null) {
            log.error("Try to get request count by EventId=null");
            throw new BadRequestException("Try to get requests count by EventIds=null");
        }
        return requestRepository.countRequestsByEventId(eventId);
    }

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        log.info("Try to get User Requests by userId={}", userId);
        checkUserExistInDB(userId);
        List<Request> requests = requestRepository.findRequestsByRequesterId(userId);
        log.info("Requests found successfully by userId={}", userId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addUserRequest(Long requesterId, Long eventId) {
        log.info("Try to make new Request");

        checkUserExistInDB(requesterId);
        checkEventExistInDB(eventId);
        checkRequestNotExistInDB(requesterId, eventId);

        Event eventProxy = entityManager.getReference(Event.class, eventId);
        User userProxy = entityManager.getReference(User.class, requesterId);

        checkRequesterIsNotOwnerEvent(requesterId, eventProxy);
        checkEventIsAbleToRequest(eventProxy);

        Status requestStatus = eventProxy.getRequestModeration() ? Status.PENDING : Status.CONFIRMED;

        LocalDateTime created = LocalDateTime.now();

        Request newRequest = new Request(null, created, eventProxy, userProxy, requestStatus);

        Request savedRequest = requestRepository.save(newRequest);
        ParticipationRequestDto requestDto = RequestMapper.toParticipationRequestDto(savedRequest);

        System.out.println("request DTO =" + requestDto);
        log.info("Save request={}", requestDto);

        return requestDto;
    }

    @Override
    public ParticipationRequestDto rejectUserRequest(Long userId, Long requestId) {
        log.info("Try to reject requestId={} by userId={}", userId, requestId);
        checkUserExistInDB(userId);
        checkRequestExistInDB(requestId);
        Optional<Request> requestOptional = requestRepository.findById(requestId);
        Request request = requestOptional.get();
        if (!request.getRequester().getId().equals(userId)) {
            log.error("Canceled request can only requestor={}", request.getRequester().getId());
            throw new ConflictDataException("Canceled request can only requestor");
        }
        request.setStatus(Status.REJECTED);
        Request savedRequest = requestRepository.save(request);
        log.info("Successfully rejected requestId={} by userId={}", userId, requestId);
        return RequestMapper.toParticipationRequestDto(savedRequest);
    }

    private void checkRequestExistInDB(Long requestId) {
        if (!requestRepository.existsById(requestId)) {
            log.error("Request was not found with id={}", requestId);
            throw new NotFoundException("Request was not found with id=" + requestId);
        }
    }

    private void checkEventIsAbleToRequest(Event event) {
        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Event={} is not published", event.getId());
            throw new ConflictDataException("Event=" + event.getId() + " is not published");
        }
        Long amountRequest = countRequestsByEventId(event.getId());
        if (amountRequest >= event.getParticipantLimit()) {
            log.error("Event={} has no available spots for participation", event.getId());
            throw new ConflictDataException("Event=" + event.getId() + " has no available spots for participation");
        }

    }

    private void checkRequesterIsNotOwnerEvent(Long requesterId, Event eventProxy) {
        if (requesterId.equals(eventProxy.getInitiator().getId())) {
            log.error("Requester={} cant make request for its event", requesterId);
            throw new ConflictDataException("Requester cant make request for its event");
        }
    }

    private void checkRequestNotExistInDB(Long requesterId, Long eventId) {
        Optional<Request> requestOptional = requestRepository.findByRequesterIdAndEventId(requesterId, eventId);
        if (requestOptional.isPresent()) {
            log.error("Request with requesterId={} and eventId={} is already in DB", requesterId, eventId);
            throw new ConflictDataException("Request with requesterId and eventId is already in DB");
        }
    }

    private void checkUserExistInDB(Long userId) {
        if (!requestRepository.existsUserById(userId)) {
            log.error("User was not found with id={}", userId);
            throw new NotFoundException("User was not found with id=" + userId);
        }
    }

    private void checkEventExistInDB(Long eventId) {
        if (!requestRepository.existsEventById(eventId)) {
            log.error("Event was not found with id={}", eventId);
            throw new NotFoundException("Event was not found with id=" + eventId);
        }
    }

    private Map<Long, Long> getRequestsByEventIds(Set<Long> eventIds) {
        List<Object[]> results = requestRepository.countRequestsByEventIds(eventIds);
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }
}

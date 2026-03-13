package ru.practicum.explorewithme.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.ConflictDataException;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.event.Event;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.model.request.Status;
import ru.practicum.explorewithme.model.user.User;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.service.event.EventService;
import ru.practicum.explorewithme.service.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    public Map<Long, Long> countRequestsByEventIds(List<Long> eventIds) {
        log.info("Try to count request by event ids={}", eventIds);
        if (eventIds == null) {
            log.error("Try to get requests count by EventIds=null");
            throw new BadRequestException("Try to get requests count by EventIds=null");
        }
        if (eventIds.isEmpty()) {
            return new HashMap<>();
        }
        return requestRepository.countRequestsByEventIds(eventIds);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        User initiator = userService.getEntityById(userId);
        Event event = eventService.getEntityById(eventId);

        if (!initiator.getId().equals(event.getInitiator().getId())) {
            throw new ConflictDataException(
                    String.format("Пользователь с id = %d не является автором события с id = %d", initiator.getId(), event.getId())
            );
        }

        return requestRepository.findByEventId(event.getId()).stream()
                .map(RequestMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public List<ParticipationRequestDto> reviewUserEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        User initiator = userService.getEntityById(userId);
        Event event = eventService.getEntityById(eventId);

        if (!initiator.getId().equals(event.getInitiator().getId())) {
            throw new ConflictDataException(
                    String.format("Пользователь с id = %d не является автором события с id = %d", initiator.getId(), event.getId())
            );
        }

        Integer confirmedEventRequests = requestRepository.countEventRequestsInSpecialStatus(event.getId(), Status.CONFIRMED.name());
        // превышел лимит заявок на событие
        if (confirmedEventRequests > event.getParticipantLimit()) {
            throw new ConflictDataException(String.format("Достигнут лимит заявок на событие с id = %d", event.getId()));
        }

        List<Request> requests = requestRepository.findByIds(request.getRequestIds());
        // Требуется подтверждение
        if (event.getRequestModeration() && event.getParticipantLimit() > 0) {
            for (Request currentRequest : requests) {
                if (confirmedEventRequests <= event.getParticipantLimit() && currentRequest.getStatus().equals(Status.PENDING)) {
                    currentRequest.setStatus(Status.CONFIRMED);
                    confirmedEventRequests++;
                } else {
                    currentRequest.setStatus(Status.CANCELED);
                }
                requestRepository.save(currentRequest);
            }
        }

        return requests.stream()
                .map(RequestMapper::mapToDto)
                .toList();
    }
}

package ru.practicum.explorewithme.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.model.request.Request;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.service.user.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;

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
        if (!userService.userExists(userId)) {
            throw new NotFoundException("User was not found with id=" + userId);
        }
        List<Request> requests = requestRepository.findRequestsByRequesterId(userId);
        return requests.stream()
                .map(RequestMapper::toParticipationRequestDto)
                .toList();
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

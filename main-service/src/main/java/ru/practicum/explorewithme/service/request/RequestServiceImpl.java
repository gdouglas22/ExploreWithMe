package ru.practicum.explorewithme.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.repository.RequestRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

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
        return requestRepository.countRequestsByEventIds(eventIds);
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
}

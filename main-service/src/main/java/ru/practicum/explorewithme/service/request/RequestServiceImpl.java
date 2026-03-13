package ru.practicum.explorewithme.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.repository.RequestRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;

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
}

package ru.practicum.explorewithme.service.request;

import java.util.Map;
import java.util.Set;

public interface RequestService {
    Map<Long, Long> countRequestsByEventIds(Set<Long> eventIds);

    Long countRequestsByEventId(Long eventId);
}

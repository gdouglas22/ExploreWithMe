package ru.practicum.explorewithme.service.request;

import java.util.List;
import java.util.Map;

public interface RequestService {
    Map<Long, Long> countRequestsByEventIds(List<Long> eventIds);
}

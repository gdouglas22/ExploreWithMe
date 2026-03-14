package ru.practicum.explorewithme.service.request;

import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RequestService {
    Map<Long, Long> countRequestsByEventIds(Set<Long> eventIds);

    Long countRequestsByEventId(Long eventId);

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addUserRequest(Long userId, Long eventId);
}

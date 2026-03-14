package ru.practicum.explorewithme.service.request;

import ru.practicum.explorewithme.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RequestService {
    Map<Long, Long> countRequestsByEventIds(Set<Long> eventIds);

    Long countRequestsByEventId(Long eventId);

    List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId);

    List<ParticipationRequestDto> reviewUserEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}

package ru.practicum.explorewithme.controller.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events/{eventId}/requests")
@RequiredArgsConstructor
public class RequestPrivateController {
    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserEventRequests(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "eventId") Long eventId
    ) {
        return requestService.getUserEventRequests(userId, eventId);
    }

    @PatchMapping
    public List<ParticipationRequestDto> reviewUserEventRequests(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "eventId") Long eventId,
            @RequestBody @Valid EventRequestStatusUpdateRequest request
    ) {
        return requestService.reviewUserEventRequests(userId, eventId, request);
    }
}

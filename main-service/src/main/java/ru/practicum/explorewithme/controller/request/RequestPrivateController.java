package ru.practicum.explorewithme.controller.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.service.request.RequestService;

import java.util.List;

@Controller
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestPrivateController {
    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getUserRequests(@PathVariable Long userId) {
        List<ParticipationRequestDto> requestDto = requestService.getUserRequests(userId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(requestDto);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addUserRequest(@PathVariable Long userId,
                                                                  @RequestParam Long eventId) {
        ParticipationRequestDto requestDto = requestService.addUserRequest(userId, eventId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(requestDto);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> updateUserRequest(@PathVariable Long userId,
                                                                     @PathVariable Long requestId) {
        ParticipationRequestDto requestDto = requestService.rejectUserRequest(userId, requestId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(requestDto);
    }

}

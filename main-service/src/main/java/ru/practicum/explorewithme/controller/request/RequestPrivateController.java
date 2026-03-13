package ru.practicum.explorewithme.controller.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
        return ResponseEntity.ok().body(requestDto);
    }

}

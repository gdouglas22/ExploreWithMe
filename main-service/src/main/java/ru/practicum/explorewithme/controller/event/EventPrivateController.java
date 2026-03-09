package ru.practicum.explorewithme.controller.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.NewEventDto;
import ru.practicum.explorewithme.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class EventPrivateController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(
            @PathVariable(value = "userId") Long userId,
            @RequestBody @Valid NewEventDto newEventDto
            ) {
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getByUserIdAndId(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "eventId") Long eventId
    ) {
        return eventService.getByUserIdAndId(userId, eventId);
    }

    @GetMapping
    public List<EventFullDto> getByUserId(
            @PathVariable(value = "userId") Long userId,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return eventService.getByUserId(userId, from, size);
    }
}

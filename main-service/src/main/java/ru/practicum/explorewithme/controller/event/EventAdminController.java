package ru.practicum.explorewithme.controller.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.event.EventAdminRequest;
import ru.practicum.explorewithme.dto.event.EventFullDto;
import ru.practicum.explorewithme.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.service.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventFullDto>> getEvents(@RequestParam List<Long> users,
                                                        @RequestParam List<String> states,
                                                        @RequestParam List<Long> categories,
                                                        @RequestParam String rangeStart,
                                                        @RequestParam String rangeEnd,
                                                        @RequestParam(defaultValue = "0") Integer from,
                                                        @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        EventAdminRequest eventAdminRequest = new EventAdminRequest(users, states, categories,
                rangeStart, rangeEnd);

        Page<EventFullDto> eventFullDtoPage = eventService.getEventByParam(eventAdminRequest, pageable);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(eventFullDtoPage);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId,
                                                    @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        EventFullDto eventFullDto = eventService.updateEventAdmin(eventId, updateEventAdminRequest);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(eventFullDto);
    }
}
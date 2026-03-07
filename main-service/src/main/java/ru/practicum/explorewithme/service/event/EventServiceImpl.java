package ru.practicum.explorewithme.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.repository.EventRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl {
    private final EventRepository eventRepository;
}

package ru.practicum.explorewithme.service;

import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.dto.EndpointHit;
import ru.practicum.explorewithme.dto.ViewStats;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.model.HitEntity;
import ru.practicum.explorewithme.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class StatsService {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private final HitRepository repository;

    public StatsService(HitRepository repository) {
        this.repository = repository;
    }

    public void saveHit(EndpointHit request) {
        HitEntity hit = new HitEntity(null, request.app(), request.uri(), request.ip(), request.timestamp());
        repository.save(hit);
    }

    public List<ViewStats> getStats(String start,
                                    String end,
                                    List<String> uris,
                                    boolean unique) {
        LocalDateTime startDateTime = parseDateTime(start, "start");
        LocalDateTime endDateTime = parseDateTime(end, "end");

        if (startDateTime.isAfter(endDateTime)) {
            throw new BadRequestException("start must be before or equal to end");
        }

        return repository.getStats(startDateTime, endDateTime, uris, unique);
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException(fieldName + " must match pattern " + DATE_TIME_PATTERN);
        }
    }
}

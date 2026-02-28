package ru.practicum.explorewithme;

import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository {
    Hit saveHit(Hit hit);

    List<HitDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}

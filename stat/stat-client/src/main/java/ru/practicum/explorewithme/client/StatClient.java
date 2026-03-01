package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.explorewithme.dto.EndpointHit;
import ru.practicum.explorewithme.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class StatClient {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;

    public StatClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void saveHit(EndpointHit hitDto) {
        restTemplate.postForObject("/hit", hitDto, Void.class);
        log.info("Сохранено обращение: {}", hitDto);
    }

    public List<ViewStats> getStat(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromPath("/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uri -> builder.queryParam("uris", uri));
        }

        ViewStats[] response = restTemplate
                .getForObject(builder.toUriString(), ViewStats[].class);

        return response != null ? Arrays.asList(response) : List.of();
    }
}
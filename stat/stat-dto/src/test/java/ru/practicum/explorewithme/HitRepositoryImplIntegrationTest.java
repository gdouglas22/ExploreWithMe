package ru.practicum.explorewithme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JdbcTest
@ActiveProfiles("test")
@ContextConfiguration(classes = {
        DataSourceAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class
})
class HitRepositoryImplIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HitRepository hitRepository;

    @BeforeEach
    void setUp() {
        this.hitRepository = new HitRepositoryImpl(jdbcTemplate);
    }

    @Test
    void shouldSaveHitSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();

        Hit savedHit = hitRepository.saveHit(hit);

        assertNotNull(savedHit.getId());
    }

    @Test
    void shouldGetStatsSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();

        hitRepository.saveHit(hit);

        List<HitDto> receivedList = hitRepository.getStats(now.minusHours(1), now.plusHours(1),
                List.of("test_uri"), false);
        HitDto receivedHitDto = receivedList.getFirst();

        assertEquals(1, receivedList.size());
        assertEquals("test_app", receivedHitDto.getApp());
        assertEquals("test_uri", receivedHitDto.getUri());
        assertEquals(1, receivedHitDto.getHits());

    }

    @Test
    void shouldGetStatsSeveralHitSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit1 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();
        Hit hit2 = Hit.builder()
                .app("test_app_hit2")
                .uri("test_uri_hit2")
                .ip("test_ip_hit2")
                .created(now.plusMinutes(10))
                .build();

        hitRepository.saveHit(hit1);
        hitRepository.saveHit(hit2);

        List<HitDto> receivedList = hitRepository.getStats(now.minusHours(1), now.plusHours(1),
                List.of("test_uri", "test_uri_hit2"), false);
        HitDto receivedHitDto = receivedList.getFirst();

        assertEquals(2, receivedList.size());
        assertEquals("test_app", receivedHitDto.getApp());
        assertEquals("test_uri", receivedHitDto.getUri());
        assertEquals(1, receivedHitDto.getHits());
    }

    @Test
    void shouldGetStatsHitWithCorrectTimeSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit1 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now.minusHours(10))
                .build();
        Hit hit2 = Hit.builder()
                .app("test_app_hit2")
                .uri("test_uri_hit2")
                .ip("test_ip_hit2")
                .created(now)
                .build();

        hitRepository.saveHit(hit1);
        hitRepository.saveHit(hit2);

        List<HitDto> receivedList = hitRepository.getStats(now.minusHours(1), now.plusHours(1),
                List.of("test_uri", "test_uri_hit2"), false);
        HitDto receivedHitDto = receivedList.getFirst();

        assertEquals(1, receivedList.size());
        assertEquals("test_app_hit2", receivedHitDto.getApp());
        assertEquals("test_uri_hit2", receivedHitDto.getUri());
        assertEquals(1, receivedHitDto.getHits());
    }

    @Test
    void shouldGetStatsHitWithCorrectUriSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit1 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();
        Hit hit2 = Hit.builder()
                .app("test_app_hit2")
                .uri("test_uri_hit2")
                .ip("test_ip_hit2")
                .created(now)
                .build();

        hitRepository.saveHit(hit1);
        hitRepository.saveHit(hit2);

        List<HitDto> receivedList = hitRepository.getStats(now.minusHours(1), now.plusHours(1),
                List.of("test_uri"), false);
        HitDto receivedHitDto = receivedList.getFirst();

        assertEquals(1, receivedList.size());
        assertEquals("test_app", receivedHitDto.getApp());
        assertEquals("test_uri", receivedHitDto.getUri());
        assertEquals(1, receivedHitDto.getHits());
    }

    @Test
    void shouldGetStatsUniqueHitSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit1 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();
        Hit hit2 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();

        hitRepository.saveHit(hit1);
        hitRepository.saveHit(hit2);

        List<HitDto> receivedList = hitRepository.getStats(now.minusHours(1), now.plusHours(1),
                List.of("test_uri"), true);
        HitDto receivedHitDto = receivedList.getFirst();

        assertEquals(1, receivedList.size());
        assertEquals("test_app", receivedHitDto.getApp());
        assertEquals("test_uri", receivedHitDto.getUri());
        assertEquals(1, receivedHitDto.getHits());
    }

    @Test
    void shouldGetStatsNotUniqueHitSuccessfully_Integration() {
        LocalDateTime now = LocalDateTime.now();
        Hit hit1 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();
        Hit hit2 = Hit.builder()
                .app("test_app")
                .uri("test_uri")
                .ip("test_ip")
                .created(now)
                .build();

        hitRepository.saveHit(hit1);
        hitRepository.saveHit(hit2);

        List<HitDto> receivedList = hitRepository.getStats(now.minusHours(1), now.plusHours(1),
                List.of("test_uri"), false);
        HitDto receivedHitDto = receivedList.getFirst();

        assertEquals(1, receivedList.size());
        assertEquals("test_app", receivedHitDto.getApp());
        assertEquals("test_uri", receivedHitDto.getUri());
        assertEquals(2, receivedHitDto.getHits());
    }
}
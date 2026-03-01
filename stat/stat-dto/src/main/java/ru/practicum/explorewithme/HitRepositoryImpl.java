package ru.practicum.explorewithme;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.dto.HitDto;
import ru.practicum.explorewithme.model.Hit;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class HitRepositoryImpl implements HitRepository {
    private static final String INSERT_QUERY = "INSERT INTO stat(app, uri, ip, created) VALUES (?, ?, ?, ?)";

    private static final String FIND_HIT_DTO_BY_PARAM_QUERY =
            "SELECT app, uri, COUNT(*) AS hits " +
                    "FROM stat " +
                    "WHERE created BETWEEN ? AND ? ";

    private static final String FIND_UNIQUE_HIT_DTO_BY_PARAM_QUERY =
            "SELECT app, uri, COUNT(DISTINCT ip) AS hits " +
                    "FROM stat " +
                    "WHERE created BETWEEN ? AND ? ";

    private final JdbcTemplate jdbc;

    @Override
    public Hit saveHit(Hit hit) {
        log.info("Попытка сохранения данных");

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbc.update(conection -> {
                PreparedStatement ps = conection.prepareStatement(INSERT_QUERY, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setObject(1, hit.getApp());
                ps.setObject(2, hit.getUri());
                ps.setObject(3, hit.getIp());
                ps.setObject(4, hit.getCreated());
                return ps;
            }, keyHolder);

            Long id = keyHolder.getKeyAs(Long.class);
            log.info("Запрос выполнен. Значение ключа: {}", id);

            if (id != null) {
                hit.setId(id);
            } else {
                log.error("Не удалось сохранить данные");
                throw new RuntimeException("Не удалось сохранить данные");
            }
        } catch (RuntimeException ex) {
            log.error("Непредвиденная ошибка");
            throw new RuntimeException("Непредвиденная ошибка");
        }

        log.info("Данные успешно добавлены. ID={}", hit.getId());
        return hit;
    }

    @Override
    public List<HitDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        log.info("Попытка получения статистики start={}, end={}, unique={}, uris={}", start, end, unique, uris);

        String baseQuery = unique
                ? FIND_UNIQUE_HIT_DTO_BY_PARAM_QUERY
                : FIND_HIT_DTO_BY_PARAM_QUERY;

        List<Object> parameters = new ArrayList<>();
        parameters.add(start);
        parameters.add(end);

        String inCondition = buildInCondition(uris, parameters);

        String fullQuery = baseQuery + inCondition +
                " GROUP BY app, uri ORDER BY hits DESC, app, uri";

        RowMapper<HitDto> mapper = (rs, rowNum) -> new HitDto(
                rs.getString("app"),
                rs.getString("uri"),
                rs.getLong("hits")
        );

        List<HitDto> hits = jdbc.query(fullQuery, mapper, parameters.toArray());
        log.info("Получен массив длиной {}", hits.size());
        return hits;
    }

    private String buildInCondition(List<String> uris, List<Object> parameters) {
        StringBuilder inBuilder = new StringBuilder("AND uri IN (");
        for (int i = 0; i < uris.size(); i++) {
            if (i > 0) {
                inBuilder.append(", ");
            }
            inBuilder.append("?");
            parameters.add(uris.get(i));
        }
        inBuilder.append(")");
        return inBuilder.toString();
    }
}

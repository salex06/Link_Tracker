package backend.academy.repository.jdbc;

import backend.academy.model.jdbc.JdbcLink;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Репозиторий, взаимодействующий с БД для получения информации о ссылках и чатах, связанных с ними */
@Repository
@RequiredArgsConstructor
public class JdbcLinkRepository {
    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    private final RowMapper<JdbcLink> jdbcLinkRowMapper = (rs, rn) -> new JdbcLink(
            rs.getLong("id"),
            rs.getString("link_value"),
            rs.getTimestamp("last_update").toInstant());

    /**
     * Сохранить ссылку в БД. Если ссылка уже записана в БД - обновляет запись
     *
     * @param jdbcLink ссылка для сохранения
     * @return сохраненную ссылку
     */
    @Transactional
    public JdbcLink save(JdbcLink jdbcLink) {
        if (jdbcLink.getId() == null) {
            return insertEntity(jdbcLink);
        }
        return updateEntity(jdbcLink);
    }

    @Transactional
    private JdbcLink insertEntity(JdbcLink jdbcLink) {
        String sql = "INSERT INTO link(link_value) VALUES (:linkValue)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkValue", jdbcLink.getUrl());

        namedJdbcTemplate.update(sql, params, keyHolder);

        Map<String, Object> keyMap = keyHolder.getKeys();
        if (keyMap != null && !keyMap.isEmpty()) {
            Long generatedId = (Long) keyMap.get("id");
            jdbcLink.setId(generatedId);
            return jdbcLink;
        }
        return null;
    }

    @Modifying
    @Transactional
    private JdbcLink updateEntity(JdbcLink jdbcLink) {
        String sql = "UPDATE link SET link_value = :linkValue, last_update = :lastUpdate WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkValue", jdbcLink.getUrl());
        params.addValue("lastUpdate", Timestamp.from(jdbcLink.getLastUpdateTime()));
        params.addValue("id", jdbcLink.getId());

        namedJdbcTemplate.update(sql, params);

        return jdbcLink;
    }

    /**
     * Получить все ссылки из БД с использованием механизма пагинации
     *
     * @param pageable информация о текущей запрашиваемой странице - размер и смещение
     * @return {@code Page<JdbcLink>} - страница с ссылками
     */
    public Page<JdbcLink> findAll(Pageable pageable, Duration duration) {
        int pageSize = pageable.getPageSize();
        long offset = pageable.getOffset();

        String sql =
                "SELECT * FROM link WHERE last_update < NOW() - cast(:duration as interval) AND id IN (SELECT link_id FROM tg_chat_link) ORDER BY id LIMIT :pageSize OFFSET :offset";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("pageSize", pageSize);
        params.addValue("offset", offset);
        params.addValue("duration", duration.getSeconds() + " seconds");

        List<JdbcLink> content = namedJdbcTemplate.query(sql, params, jdbcLinkRowMapper);
        String countSql =
                "SELECT COUNT(*) FROM link WHERE last_update < NOW() - cast(:duration as interval) AND id IN (SELECT link_id FROM tg_chat_link)";

        Long total = namedJdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null) {
            throw new RuntimeException("Unexpected Sql Error");
        }

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Получить ссылку по значению url
     *
     * @param url значение ссылки для поиска
     * @return ссылку, если найдена, иначе - {@code Optional.empty()}
     */
    public Optional<JdbcLink> getLinkByUrl(String url) {
        String sql = "SELECT * FROM link WHERE link_value = :url";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("url", url);

        try {
            JdbcLink link = namedJdbcTemplate.queryForObject(sql, params, jdbcLinkRowMapper);
            return Optional.of(link);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Получить все чаты, отслеживающие ссылку с данным значением
     *
     * @param url значение ссылки
     * @return множество идентификаторов чатов, отслеживающих ссылку
     */
    public Set<Long> getChatIdsByUrl(String url) {
        String sql =
                "SELECT tg_chat_id FROM tg_chat_link INNER JOIN link ON link_id = id WHERE link_value = :linkValue";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkValue", url);

        return new HashSet<>(namedJdbcTemplate.queryForList(sql, params, Long.class));
    }

    /**
     * Получить ссылку по её значению и идентификатору чата
     *
     * @param chatId идентификатор чата
     * @param url значение ссылки
     * @return {@code Optional<JdbcLink>}, если ссылка, чат найдены и связаны друг с другом, иначе -
     *     {@code Optional.empty()}
     */
    public Optional<JdbcLink> getLinkByUrlAndChatId(Long chatId, String url) {
        String sql =
                "SELECT * FROM link JOIN tg_chat_link ON tg_chat_link.link_id = link.id WHERE link_value = :linkValue AND tg_chat_id = :chatId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkValue", url);
        params.addValue("chatId", chatId);

        try {
            JdbcLink link = namedJdbcTemplate.queryForObject(sql, params, jdbcLinkRowMapper);
            return Optional.of(link);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Получить все ссылки, отслеживаемые чатом
     *
     * @param chatId идентификатор чата
     * @return список ссылок, которые отслеживает чат
     */
    public List<JdbcLink> getAllLinksByChatId(Long chatId) {
        String sql = "SELECT link.id, link.link_value, link.last_update FROM link "
                + "INNER JOIN tg_chat_link ON link.id = tg_chat_link.link_id "
                + "WHERE tg_chat_link.tg_chat_id = :chatId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);

        return namedJdbcTemplate.query(sql, params, jdbcLinkRowMapper);
    }

    /**
     * Обновить ссылку
     *
     * @param id идентификатор ссылки
     * @param url значение ссылки (поле обновляется)
     * @param lastUpdateTime время последнего обновления (поле обновляется)
     */
    @Modifying
    @Transactional
    public void updateLink(Long id, String url, Instant lastUpdateTime) {
        String sql = "UPDATE link SET link_value = :linkValue, last_update = :lastUpdate WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkValue", url);
        params.addValue("id", id);
        params.addValue("lastUpdate", Timestamp.from(lastUpdateTime));

        namedJdbcTemplate.update(sql, params);
    }
}

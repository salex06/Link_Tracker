package backend.academy.repository.jdbc;

import backend.academy.model.jdbc.JdbcTgChat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcChatRepository {
    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    private final RowMapper<JdbcTgChat> jdbcChatRowMapper =
            (rs, rn) -> new JdbcTgChat(rs.getLong("id"), rs.getLong("chat_id"));

    public JdbcTgChat save(JdbcTgChat jdbcTgChat) {
        if (jdbcTgChat.getId() == null) {
            return insertEntity(jdbcTgChat);
        }
        return jdbcTgChat;
    }

    @Modifying
    @Transactional
    private JdbcTgChat insertEntity(JdbcTgChat jdbcTgChat) {
        String sql = "INSERT INTO tg_chat(chat_id) VALUES (:chat_id)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chat_id", jdbcTgChat.getChatId());

        namedJdbcTemplate.update(sql, params, keyHolder);

        Map<String, Object> keyMap = keyHolder.getKeys();
        if (keyMap != null && !keyMap.isEmpty()) {
            Long generatedId = (Long) keyMap.get("id");
            jdbcTgChat.setId(generatedId);
            return jdbcTgChat;
        }
        return null;
    }

    @Transactional
    public boolean existsByChatId(Long chatId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM tg_chat chat WHERE chat.chat_id = :chatId)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);

        return Boolean.TRUE.equals(namedJdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Transactional
    public Optional<JdbcTgChat> findByChatId(Long chatId) {
        String sql = "SELECT * FROM tg_chat chat WHERE chat.chat_id = :chatId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);

        try {
            JdbcTgChat link = namedJdbcTemplate.queryForObject(sql, params, jdbcChatRowMapper);
            return Optional.of(link);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<JdbcTgChat> findById(Long id) {
        String sql = "SELECT * FROM tg_chat chat WHERE chat.id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        try {
            JdbcTgChat link = namedJdbcTemplate.queryForObject(sql, params, jdbcChatRowMapper);
            return Optional.of(link);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Modifying
    @Transactional
    public void deleteByChatId(Long chatId) {
        String sql = "DELETE FROM tg_chat chat WHERE chat.chat_id = :chatId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);

        namedJdbcTemplate.update(sql, params);
    }

    @Transactional
    public List<JdbcTgChat> getChatsByLink(Long linkId) {
        String sql =
                "SELECT id, chat_id FROM tg_chat_link INNER JOIN tg_chat ON tg_chat.id = tg_chat_id WHERE link_id = :linkId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);

        return namedJdbcTemplate.query(sql, params, jdbcChatRowMapper);
    }

    @Transactional
    public List<String> getTags(Long linkId, Long chatId) {
        String sql = "SELECT tag_value FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);

        return namedJdbcTemplate.queryForList(sql, params, String.class);
    }

    @Transactional
    public List<String> getFilters(Long linkId, Long chatId) {
        String sql = "SELECT filter_value FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);

        return namedJdbcTemplate.queryForList(sql, params, String.class);
    }

    /**
     * Связать ссылку с отслеживающим её чатом
     *
     * @param chatId идентификатор чата
     * @param linkId идентификатор ссылки
     * @return {@code true}, если чат и ссылка успешно связаны, иначе - {@code false} (чат или ссылка не найдены и т.п.)
     */
    @Modifying
    @Transactional
    public boolean saveTheChatLink(Long chatId, Long linkId) {
        try {
            String sql = "INSERT INTO tg_chat_link VALUES (:chatId, :linkId)";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("linkId", linkId);
            params.addValue("chatId", chatId);

            namedJdbcTemplate.update(sql, params);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    /**
     * Удалить связь между чатом и ссылкой
     *
     * @param chatId идентификатор чата
     * @param linkId идентификатор ссылки
     */
    @Modifying
    @Transactional
    public void removeTheChatLink(Long chatId, Long linkId) {
        String sql = "DELETE FROM tg_chat_link WHERE tg_chat_id = :chatId AND link_id = :linkId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);

        namedJdbcTemplate.update(sql, params);
    }

    @Modifying
    @Transactional
    public boolean saveTag(Long linkId, Long chatId, String tagValue) {
        try {
            String sql = "INSERT INTO chat_link_tags VALUES (:linkId, :tagValue, :chatId) ON CONFLICT DO NOTHING";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("linkId", linkId);
            params.addValue("chatId", chatId);
            params.addValue("tagValue", tagValue);

            namedJdbcTemplate.update(sql, params);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Modifying
    @Transactional
    public boolean saveFilter(Long linkId, Long chatId, String filterValue) {
        try {
            String sql = "INSERT INTO chat_link_filters VALUES (:linkId, :filterValue, :chatId)";

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("linkId", linkId);
            params.addValue("chatId", chatId);
            params.addValue("filterValue", filterValue);

            namedJdbcTemplate.update(sql, params);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Modifying
    @Transactional
    public void removeTag(Long linkId, Long chatId, String tagValue) {
        String sql =
                "DELETE FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId AND tag_value = :tagValue";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);
        params.addValue("tagValue", tagValue);

        namedJdbcTemplate.update(sql, params);
    }

    @Modifying
    @Transactional
    public void removeAllTags(Long linkId, Long chatId) {
        String sql = "DELETE FROM chat_link_tags WHERE chat_id = :chatId AND link_id = :linkId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);

        namedJdbcTemplate.update(sql, params);
    }

    @Modifying
    @Transactional
    public void removeFilter(Long linkId, Long chatId, String filterValue) {
        String sql =
                "DELETE FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId AND filter_value = :filterValue";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);
        params.addValue("filterValue", filterValue);

        namedJdbcTemplate.update(sql, params);
    }

    @Modifying
    @Transactional
    public void removeAllFilters(Long linkId, Long chatId) {
        String sql = "DELETE FROM chat_link_filters WHERE chat_id = :chatId AND link_id = :linkId";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("linkId", linkId);
        params.addValue("chatId", chatId);

        namedJdbcTemplate.update(sql, params);
    }
}

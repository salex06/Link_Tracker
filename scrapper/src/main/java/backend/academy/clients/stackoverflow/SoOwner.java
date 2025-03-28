package backend.academy.clients.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Класс представляет модель пользователя на StackOverflow
 *
 * @param user_id идентификатор пользователя
 * @param name имя пользователя
 * @param linkToOwner ссылка на пользователя
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoOwner(
        @JsonProperty("user_id") Long user_id,
        @JsonProperty("display_name") String name,
        @JsonProperty("link") String linkToOwner) {}

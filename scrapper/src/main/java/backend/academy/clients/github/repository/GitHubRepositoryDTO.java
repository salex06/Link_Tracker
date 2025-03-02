package backend.academy.clients.github.repository;

import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Модель репозитория GitHub
 *
 * @param linkValue ссылка на репозиторий
 * @param repositoryName название репозитория
 * @param owner владелец репозитория
 * @param pushedAt время добавления нового коммита в репозиторий
 * @param updatedAt время обновления репозитория
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepositoryDTO(
        @JsonProperty("html_url") String linkValue,
        @JsonProperty("name") String repositoryName,
        @JsonProperty("owner") GitHubUser owner,
        @JsonProperty("pushed_at") LocalDateTime pushedAt,
        @JsonProperty("updated_at") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime updatedAt) {
    /** Конвертер времени в формате timestamp (из JSON-объекта) в объект класса LocalDateTime */
    public static class TimeStampToLocalDateTimeConverter extends StdConverter<String, LocalDateTime> {
        /**
         * Конвертировать timestamp в LocalDateTime
         *
         * @param time время в строковом виде
         * @return объекта класса {@code LocalDateTime} - сконвертированное время
         */
        @Override
        public LocalDateTime convert(String time) {
            Instant instant = Instant.parse(time);
            return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
    }
}

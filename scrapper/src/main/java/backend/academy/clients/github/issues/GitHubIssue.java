package backend.academy.clients.github.issues;

import backend.academy.clients.github.GitHubUser;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Модель задачи (issue) GitHub
 *
 * @param linkValue ссылка на issue
 * @param title название
 * @param author автор
 * @param updatedAt время обновления
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubIssue(
        @JsonProperty("html_url") String linkValue,
        @JsonProperty("title") String title,
        @JsonProperty("user") GitHubUser author,
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

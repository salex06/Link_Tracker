package backend.academy.clients.stackoverflow.questions;

import backend.academy.clients.stackoverflow.SoOwner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Объект передачи данных, представляющий отедльную запись - вопрос на StackOverflow
 *
 * @param owner автор вопроса
 * @param lastActivity дата последней активности
 * @param creationDate дата создания
 * @param lastEditDate дата последнего изменения
 * @param title заголовок вопроса
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoQuestionDTO(
        @JsonProperty("owner") SoOwner owner,
        @JsonProperty("last_activity_date") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastActivity,
        @JsonProperty("creation_date") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime creationDate,
        @JsonProperty("last_edit_date") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastEditDate,
        @JsonProperty("title") String title) {

    public static class TimeStampToLocalDateTimeConverter extends StdConverter<Long, LocalDateTime> {
        @Override
        public LocalDateTime convert(Long seconds) {
            if (seconds == null) {
                return LocalDateTime.MIN;
            }

            return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("UTC"));
        }
    }
}

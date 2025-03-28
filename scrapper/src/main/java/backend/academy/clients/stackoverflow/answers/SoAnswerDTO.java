package backend.academy.clients.stackoverflow.answers;

import backend.academy.clients.stackoverflow.SoOwner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Объект передачи данных, определяющий структуру отдельной записи - конкретного ответа на StackOverflow
 *
 * @param owner автор ответа
 * @param lastActivity дата последней активности по ответу
 * @param creationDate дата создания ответа
 * @param lastEditDate дата изменения ответа
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoAnswerDTO(
        @JsonProperty("owner") SoOwner owner,
        @JsonProperty("last_activity_date") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastActivity,
        @JsonProperty("creation_date") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime creationDate,
        @JsonProperty("last_edit_date") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastEditDate) {

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

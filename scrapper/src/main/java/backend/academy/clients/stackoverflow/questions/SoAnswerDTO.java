package backend.academy.clients.stackoverflow.questions;

import backend.academy.clients.converter.JsonConverters;
import backend.academy.clients.stackoverflow.SoOwner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;

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
        @JsonProperty("last_activity_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastActivity,
        @JsonProperty("creation_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime creationDate,
        @JsonProperty("last_edit_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastEditDate,
        @JsonProperty("body") @JsonDeserialize(converter = JsonConverters.StringTruncator.class) String text) {}

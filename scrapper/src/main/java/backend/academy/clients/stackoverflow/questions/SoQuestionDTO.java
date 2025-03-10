package backend.academy.clients.stackoverflow.questions;

import backend.academy.clients.converter.JsonConverters;
import backend.academy.clients.stackoverflow.SoOwner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;

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
        @JsonProperty("last_activity_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastActivity,
        @JsonProperty("creation_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime creationDate,
        @JsonProperty("last_edit_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastEditDate,
        @JsonProperty("title") String title) {}

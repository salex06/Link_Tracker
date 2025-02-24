package backend.academy.clients.stackoverflow.answers;

import backend.academy.clients.stackoverflow.SoOwner;
import backend.academy.clients.stackoverflow.questions.SoQuestionDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SoAnswerDTO(
        @JsonProperty("owner") SoOwner owner,
        @JsonProperty("last_activity_date")
                @JsonDeserialize(converter = SoQuestionDTO.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime lastActivity,
        @JsonProperty("creation_date")
                @JsonDeserialize(converter = SoQuestionDTO.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime creationDate,
        @JsonProperty("last_edit_date")
                @JsonDeserialize(converter = SoQuestionDTO.TimeStampToLocalDateTimeConverter.class)
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

package backend.academy.clients.stackoverflow.questions;

import backend.academy.clients.converter.JsonConverters;
import backend.academy.clients.stackoverflow.SoOwner;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;

/**
 * Модель комментария в системе
 *
 * @param owner автор комментария
 * @param createdAt дата создания комментария
 * @param postId идентификатор темы
 * @param commentId идентификатор комментария
 * @param text содержимое комментария
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SoCommentDTO(
        @JsonProperty("owner") SoOwner owner,
        @JsonProperty("creation_date")
                @JsonDeserialize(converter = JsonConverters.TimeStampToLocalDateTimeConverter.class)
                LocalDateTime createdAt,
        @JsonProperty("post_id") Long postId,
        @JsonProperty("comment_id") Long commentId,
        @JsonProperty("body_markdown") @JsonDeserialize(converter = JsonConverters.StringTruncator.class)
                String text) {}

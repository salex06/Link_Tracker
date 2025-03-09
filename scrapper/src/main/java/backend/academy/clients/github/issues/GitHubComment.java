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
 * Модель комментария в issue или pull request на GitHub
 *
 * @param id идентификатор комментария
 * @param url ссылка на комментарий
 * @param user автор комментария
 * @param createdAt дата создания комментария
 * @param body тело комментария
 * @param issueUrl issue или pull request, на которые оставлен комментарий
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubComment(
        @JsonProperty("id") Long id,
        @JsonProperty("html_url") String url,
        @JsonProperty("user") GitHubUser user,
        @JsonProperty("created_at") @JsonDeserialize(converter = TimeStampToLocalDateTimeConverter.class)
                LocalDateTime createdAt,
        @JsonProperty("body") @JsonDeserialize(converter = StringTruncator.class) String body,
        @JsonProperty("issue_url") String issueUrl) {
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

    /** Преобразователь тела комментария в превью */
    public static class StringTruncator extends StdConverter<String, String> {
        /**
         * Усечь слишком большой комментарий до требуемого количества символов, чтобы превью было удобно читать
         *
         * @param body тело комментария
         * @return усеченное содержимое комментария
         */
        @Override
        public String convert(String body) {
            // TODO: magic number убрать (200 - максимальная длина превью по ФТ)
            return body.substring(0, Math.min(body.length(), 200));
        }
    }
}

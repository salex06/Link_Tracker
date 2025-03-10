package backend.academy.clients.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class JsonConverters {
    private JsonConverters() {}
    /** Конвертер времени из JSON-объекта в объект класса LocalDateTime */
    public static class LocalDateTimeConverter extends StdConverter<String, LocalDateTime> {
        /**
         * Конвертировать в LocalDateTime
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

    /** Конвертер времени в формате timestamp (из JSON-объекта) в объект класса LocalDateTime */
    public static class TimeStampToLocalDateTimeConverter extends StdConverter<Long, LocalDateTime> {
        /**
         * Конвертировать timestamp в LocalDateTime
         *
         * @param seconds количество секунд от 1 янв 1970 (fromEpochSeconds)
         * @return объекта класса {@code LocalDateTime} - сконвертированное время
         */
        @Override
        public LocalDateTime convert(Long seconds) {
            if (seconds == null) {
                return LocalDateTime.MIN;
            }
            return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.of("UTC"));
        }
    }
}

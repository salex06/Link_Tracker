package backend.academy.clients.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;

public final class JsonConverters {
    private JsonConverters() {}

    public static class InstantTimeConverter extends StdConverter<String, Instant> {
        /**
         * Конвертировать в Instant
         *
         * @param time время в строковом виде
         * @return объекта класса {@code LocalDateTime} - сконвертированное время
         */
        @Override
        public Instant convert(String time) {
            return Instant.parse(time);
        }
    }

    public static class StringTruncator extends StdConverter<String, String> {
        /**
         * Усечь слишком большой комментарий до требуемого количества символов, чтобы превью было удобно читать
         *
         * @param body тело комментария
         * @return усеченное содержимое комментария
         */
        @Override
        public String convert(String body) {
            return body.substring(0, Math.min(body.length(), 200));
        }
    }

    public static class TimeStampToInstantConverter extends StdConverter<Long, Instant> {
        /**
         * Конвертировать timestamp в LocalDateTime
         *
         * @param seconds количество секунд от 1 янв 1970 (fromEpochSeconds)
         * @return объекта класса {@code LocalDateTime} - сконвертированное время
         */
        @Override
        public Instant convert(Long seconds) {
            if (seconds == null) {
                return Instant.MIN;
            }
            return Instant.ofEpochSecond(seconds);
        }
    }
}

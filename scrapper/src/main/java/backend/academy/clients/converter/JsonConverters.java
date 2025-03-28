package backend.academy.clients.converter;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.time.Instant;

public final class JsonConverters {
    private JsonConverters() {}
    /** Конвертер времени из JSON-объекта в объект класса Instant */
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

    /** Конвертер времени в формате timestamp (из JSON-объекта) в объект класса Instant */
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

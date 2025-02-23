package backend.academy.dto;

import java.util.List;

/**
 * DTO для хранения ошибок обработки некорректных запросов
 *
 * @param description описание ошибки
 * @param code код ошибки
 * @param exceptionName имя сгенерированного исключения
 * @param exceptionMessage сообщение сгенерированного исключения
 * @param trace трассировка стэка
 */
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> trace) {
    public ApiErrorResponse() {
        this(null, null, null, null, null);
    }
}

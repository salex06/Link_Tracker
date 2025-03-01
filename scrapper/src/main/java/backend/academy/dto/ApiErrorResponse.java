package backend.academy.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Объект передачи данных для ответа на некорректный запрос
 *
 * @param description описание ошибки
 * @param code код ошибки
 * @param exceptionName название исключения
 * @param exceptionMessage описание исключения
 * @param stacktrace трассировка стека
 */
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {
    public ApiErrorResponse() {
        this("", "", "", "", new ArrayList<>());
    }
}

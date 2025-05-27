package backend.academy.api.advice;

import backend.academy.dto.ApiErrorResponse;
import backend.academy.exceptions.ApiErrorException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class BotControllerAdvice {
    private final Counter errorCounter;

    public BotControllerAdvice(MeterRegistry meterRegistry) {
        errorCounter = Counter.builder("http.requests.failed")
                .description("Number of HTTP requests that resulted in an error")
                .tags("service", "bot")
                .register(meterRegistry);
    }

    /**
     * Обрабатывает генерируемое внутри контроллера исключение, обозначающее, что переданный запрос некорректен (нет
     * необходимых полей, неправильные значения полей и т.д.)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ApiErrorResponse> handleApiErrorException(@NotNull ApiErrorException ex) {
        return handleIncorrectRequest("Некорректные параметры запроса", ex, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключение "Не поддерживается метод" (код 405)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(
            @NotNull HttpRequestMethodNotSupportedException ex) {
        return handleIncorrectRequest("HTTP метод не поддерживается", ex, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Обрабатывает исключение "Ресурс не найден" (код 404)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(@NotNull NoResourceFoundException ex) {
        return handleIncorrectRequest("Отсутствует эндпоинт", ex, HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает исключение "Ошибка чтения запроса/не переданы требуемые параметры" (код 400)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadableException(
            @NotNull HttpMessageNotReadableException ex) {
        return handleIncorrectRequest("Не переданы необходимые данные", ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiErrorResponse> handleRequestNotPermitted(@NotNull RequestNotPermitted ex) {
        return handleIncorrectRequest("Ошибка. Превышен лимит запросов", ex, HttpStatus.TOO_MANY_REQUESTS);
    }

    private ResponseEntity<ApiErrorResponse> handleIncorrectRequest(
            String message, Exception ex, HttpStatusCode status) {
        errorCounter.increment();
        log.atError()
                .setMessage(message)
                .addKeyValue("status", status.value())
                .addKeyValue("error", ex)
                .log();

        return new ResponseEntity<>(
                new ApiErrorResponse(
                        "Некорректные параметры запроса",
                        String.valueOf(status.value()),
                        ex.getClass().getSimpleName(),
                        ex.getMessage(),
                        Arrays.stream(ex.getStackTrace())
                                .map(StackTraceElement::toString)
                                .toList()),
                status);
    }
}

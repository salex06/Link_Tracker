package backend.academy.api.advice;

import backend.academy.dto.ApiErrorResponse;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Класс предоставляет набор метоодов, которые перехватывают и обрабатывают исключения, сгенерированные в контроллере
 * ChatController
 */
@Slf4j
@ControllerAdvice
public class ApplicationExceptionHandler {
    /**
     * Обрабатывает ошибки чтения тела запроса (код 400)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(
            @NotNull HttpMessageNotReadableException ex) {
        return handleIncorrectRequest("Ошибка чтения тела запроса", ex, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает ошибки преобразования типов (код 400)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatchException(
            @NotNull MethodArgumentTypeMismatchException ex) {
        return handleIncorrectRequest("Ошибка преобразования типов", ex, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения вида "не поддерживается тип HTTP метода" (код 405)
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
     * Обрабатывает исключения вида "ресурс (эндпоинт) не найден" (код 404)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(@NotNull NoResourceFoundException ex) {
        return handleIncorrectRequest("Не найден подходящий эндпоинт", ex, HttpStatus.NOT_FOUND);
    }

    /**
     * Обрабатывает ошибки передачи заголовков (код 400)
     *
     * @param ex объект класса исключения
     * @return {@code ResponseEntity<ApiErrorResponse>} - форматированный ответ на некорректный запрос
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeaderException(
            @NotNull MissingRequestHeaderException ex) {
        return handleIncorrectRequest("Ошибка передачи заголовков", ex, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiErrorResponse> handleIncorrectRequest(
            String errorMessage, Exception ex, HttpStatusCode status) {
        log.atError()
                .setMessage(errorMessage)
                .addKeyValue("error-message", ex.getMessage())
                .addKeyValue("status", status)
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

package backend.academy.api.advice;

import backend.academy.dto.ApiErrorResponse;
import jakarta.validation.constraints.NotNull;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApplicationExceptionHandler {
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeaderException(
            @NotNull MissingRequestHeaderException ex) {
        return handleIncorrectRequest(ex, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiErrorResponse> handleIncorrectRequest(Exception ex, HttpStatusCode status) {
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

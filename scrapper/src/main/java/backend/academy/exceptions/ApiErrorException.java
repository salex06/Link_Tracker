package backend.academy.exceptions;

import backend.academy.dto.ApiErrorResponse;
import lombok.Getter;

/** Класс исключения для ошибок взаимодействия Bot и Scrapper (ошибки Api) */
@Getter
public class ApiErrorException extends RuntimeException {
    private final ApiErrorResponse apiErrorResponse;

    public ApiErrorException(ApiErrorResponse apiErrorResponse) {
        this.apiErrorResponse = apiErrorResponse;
    }
}

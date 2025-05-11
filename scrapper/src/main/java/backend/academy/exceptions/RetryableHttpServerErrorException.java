package backend.academy.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class RetryableHttpServerErrorException extends RuntimeException {
    private final HttpStatusCode code;
    private final String message;

    public RetryableHttpServerErrorException(HttpStatusCode code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}

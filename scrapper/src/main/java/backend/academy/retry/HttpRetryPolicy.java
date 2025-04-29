package backend.academy.retry;

import backend.academy.config.properties.ApplicationStabilityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.web.client.HttpServerErrorException;

@RequiredArgsConstructor
public class HttpRetryPolicy implements RetryPolicy {
    private final ApplicationStabilityProperties stabilityProperties;

    @Override
    public boolean canRetry(RetryContext retryContext) {
        Throwable exception = retryContext.getLastThrowable();

        return (exception == null || isAllowedException(exception)) && retryContext.getRetryCount() < getMaxAttempts();
    }

    @Override
    public RetryContext open(RetryContext parent) {
        return new RetryContextSupport(parent);
    }

    @Override
    public void close(RetryContext context) {}

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {
        ((RetryContextSupport) context).registerThrowable(throwable);
    }

    @Override
    public int getMaxAttempts() {
        return stabilityProperties.getRetry().getMaxAttempts();
    }

    private boolean isAllowedException(Throwable throwable) {
        if (throwable instanceof HttpServerErrorException ex) {
            Integer code = ex.getStatusCode().value();
            return stabilityProperties.getRetry().getHttpCodes().contains(code);
        }
        return false;
    }
}

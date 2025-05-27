package backend.academy.api.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestCounterFilter implements Filter {
    private final Counter totalRequestsCounter;
    private final Timer timer;

    @Autowired
    public RequestCounterFilter(MeterRegistry meterRegistry) {
        this.totalRequestsCounter = Counter.builder("http.requests.total")
                .description("Total number of HTTP requests")
                .tags("service", "scrapper")
                .register(meterRegistry);

        this.timer = Timer.builder("http.requests.duration")
                .publishPercentileHistogram(true)
                .description("Time taken to process the request")
                .tags("service", "scrapper")
                .register(meterRegistry);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        totalRequestsCounter.increment();

        long start = System.nanoTime();
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            timer.record(Duration.ofNanos(System.nanoTime() - start));
        }
    }
}

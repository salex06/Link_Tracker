package backend.academy.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ScrapeDurationAspect {
    private final Timer githubScrapeTimer;
    private final Timer stackoverflowScrapeTimer;

    public ScrapeDurationAspect(MeterRegistry meterRegistry) {
        githubScrapeTimer = Timer.builder("scrape.duration")
                .tag("type", "github")
                .publishPercentileHistogram(true)
                .description("Duration of the GitHub pages scrape")
                .register(meterRegistry);

        stackoverflowScrapeTimer = Timer.builder("scrape.duration")
                .tag("type", "stackoverflow")
                .publishPercentileHistogram(true)
                .description("Duration of the Stackoverflow questions scrape")
                .register(meterRegistry);
    }

    @Around("execution(* backend.academy.clients.github.GitHubClient.getUpdates(..))")
    public Object recordGitHubScrape(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } finally {
            long end = System.nanoTime() - start;
            githubScrapeTimer.record(end, TimeUnit.NANOSECONDS);
        }

        return result;
    }

    @Around("execution(* backend.academy.clients.stackoverflow.StackoverflowClient.getUpdates(..))")
    public Object recordStackoverflowScrape(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } finally {
            long end = System.nanoTime() - start;
            stackoverflowScrapeTimer.record(end, TimeUnit.NANOSECONDS);
        }

        return result;
    }
}

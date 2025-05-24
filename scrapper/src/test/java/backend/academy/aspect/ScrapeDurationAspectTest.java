package backend.academy.aspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScrapeDurationAspectTest {
    private ScrapeDurationAspect aspect;

    @Test
    public void testMetricsWereRegistered() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        aspect = new ScrapeDurationAspect(meterRegistry);

        Timer ghTimer =
                meterRegistry.find("scrape.duration").tag("type", "github").timer();
        Timer soTimer = meterRegistry
                .find("scrape.duration")
                .tag("type", "stackoverflow")
                .timer();

        assertNotNull(ghTimer);
        assertNotNull(soTimer);
    }

    @Test
    public void recordGitHubScrape_WhenCalled_ThenRecordScrapeTime() throws Throwable {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        aspect = new ScrapeDurationAspect(meterRegistry);
        Timer ghTimer =
                meterRegistry.find("scrape.duration").tag("type", "github").timer();
        ProceedingJoinPoint mockedPoint = Mockito.mock(ProceedingJoinPoint.class);
        when(mockedPoint.proceed()).thenAnswer(invocationOnMock -> {
            try {
                Thread.sleep(Duration.ofMillis(100).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return new Object();
        });

        Object res = aspect.recordGitHubScrape(mockedPoint);

        assertNotNull(ghTimer);
        assertTrue(ghTimer.count() > 0);
    }

    @Test
    public void recordStackoverflowScrape_WhenCalled_ThenRecordScrapeTime() throws Throwable {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        aspect = new ScrapeDurationAspect(meterRegistry);
        Timer soTimer = meterRegistry
                .find("scrape.duration")
                .tag("type", "stackoverflow")
                .timer();
        ProceedingJoinPoint mockedPoint = Mockito.mock(ProceedingJoinPoint.class);
        when(mockedPoint.proceed()).thenAnswer(invocationOnMock -> {
            try {
                Thread.sleep(Duration.ofMillis(100).toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return new Object();
        });

        Object res = aspect.recordStackoverflowScrape(mockedPoint);

        assertNotNull(soTimer);
        assertTrue(soTimer.count() > 0);
    }
}

package backend.academy.metrics;

import backend.academy.service.LinkService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLinksMetrics {
    public DatabaseLinksMetrics(MeterRegistry meterRegistry, LinkService linkService) {
        Supplier<Number> gitHubLinkCount = linkService::getActiveGitHubLinkCount;
        Supplier<Number> stackoverflowLinkCount = linkService::getActiveStackoverflowLinkCount;

        Gauge.builder("database.links.total", gitHubLinkCount)
                .tag("type", "github")
                .description("Number of active GitHub links in the database")
                .register(meterRegistry);

        Gauge.builder("database.links.total", stackoverflowLinkCount)
                .tag("type", "stackoverflow")
                .description("Number of active Stackoverflow links in the database")
                .register(meterRegistry);
    }
}

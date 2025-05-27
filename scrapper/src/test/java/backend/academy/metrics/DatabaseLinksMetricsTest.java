package backend.academy.metrics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.service.LinkService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatabaseLinksMetricsTest {
    private DatabaseLinksMetrics databaseLinksMetrics;

    private LinkService linkService;

    @Test
    public void testGitHubDatabaseLinksMetricsRegistrationAndValue() {
        linkService = Mockito.mock(LinkService.class);
        when(linkService.getActiveGitHubLinkCount()).thenReturn(50);
        MeterRegistry registry = new SimpleMeterRegistry();
        databaseLinksMetrics = new DatabaseLinksMetrics(registry, linkService);

        Gauge ghGauge =
                registry.find("database.links.total").tag("type", "github").gauge();

        assertNotNull(ghGauge);
        assertEquals(50, ghGauge.value(), 0.01);
        verify(linkService, times(1)).getActiveGitHubLinkCount();
    }

    @Test
    public void testStackoverflowDatabaseLinksMetricsRegistrationAndValue() {
        linkService = Mockito.mock(LinkService.class);
        when(linkService.getActiveStackoverflowLinkCount()).thenReturn(75);
        MeterRegistry registry = new SimpleMeterRegistry();
        databaseLinksMetrics = new DatabaseLinksMetrics(registry, linkService);

        Gauge ghGauge = registry.find("database.links.total")
                .tag("type", "stackoverflow")
                .gauge();

        assertNotNull(ghGauge);
        assertEquals(75, ghGauge.value(), 0.01);
        verify(linkService, times(1)).getActiveStackoverflowLinkCount();
    }
}

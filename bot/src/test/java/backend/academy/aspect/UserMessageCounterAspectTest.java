package backend.academy.aspect;

import static org.junit.jupiter.api.Assertions.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserMessageCounterAspectTest {
    private static final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private UserMessageCounterAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new UserMessageCounterAspect(meterRegistry);
    }

    @Test
    public void meterRegistryContainsCounter() {
        Counter counter = meterRegistry.find("bot.user.messages.total").counter();

        assertNotNull(counter);
    }

    @Test
    public void incrementUserMessageCounter_WhenAspectCalled_ThenIncrementCounter() {
        Counter counter = meterRegistry.find("bot.user.messages.total").counter();

        aspect.incrementUserMessageCounter();

        assertNotNull(counter);
        assertEquals(1.0, counter.count(), 0.001);
    }
}

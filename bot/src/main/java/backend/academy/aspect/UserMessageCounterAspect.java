package backend.academy.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class UserMessageCounterAspect {
    private final Counter counter;

    public UserMessageCounterAspect(MeterRegistry meterRegistry) {
        counter = Counter.builder("bot.user.messages.total")
                .description("Number of user messages")
                .register(meterRegistry);
    }

    @Before("execution(* backend.academy.processor.Processor.process(..))")
    public void incrementUserMessageCounter() {
        counter.increment();
    }
}

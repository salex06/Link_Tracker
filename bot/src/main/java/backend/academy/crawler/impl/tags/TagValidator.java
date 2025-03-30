package backend.academy.crawler.impl.tags;

import java.util.List;
import java.util.Map;

public interface TagValidator {
    boolean isStartMessage(String message);

    default boolean dialogStateWasNotSetYet(Map<Long, Map.Entry<Boolean, List<String>>> userStates, Long id) {
        return !userStates.containsKey(id);
    }

    boolean isCorrectStartMessage(String message);

    boolean dialogWasCompletedSuccessfully(Map<Long, Map.Entry<Boolean, List<String>>> userStates, Long id);
}

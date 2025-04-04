package backend.academy.crawler.impl.tags.add;

import backend.academy.crawler.impl.tags.TagValidator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component("addTagValidator")
public class AddTagValidator implements TagValidator {
    @Override
    public boolean isStartMessage(String message) {
        return message.equals("/addtag");
    }

    @Override
    public boolean isCorrectStartMessage(String message) {
        return message.matches("^/addtag [a-zA-Z0-9-_]+$");
    }

    @Override
    public boolean dialogWasCompletedSuccessfully(Map<Long, Map.Entry<Boolean, List<String>>> userStates, Long id) {
        return userStates.containsKey(id)
                && userStates.get(id).getKey()
                && userStates.get(id).getValue().size() == MESSAGE_COUNT_IF_COMPLETED;
    }

    private static final int MESSAGE_COUNT_IF_COMPLETED = 2;
}

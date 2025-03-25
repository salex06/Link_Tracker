package backend.academy.crawler;

import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.COMPLETED;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_FILTERS;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_TAGS;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CrawlValidator {
    public static final String START_DIALOG_COMMAND = "/track";
    public static final String RESTART_MESSAGE = "Сбросить";
    public static final String SKIP_THE_SETTING = "Пропустить";

    private CrawlValidator() {}

    public static boolean filtersAreValid(String text) {
        return text.matches("^(\\w+:\\w+)( \\w+:\\w+)*$");
    }

    public static boolean dialogStateWasNotSetYet(
            Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates, Long chatId) {
        return !userStates.containsKey(chatId);
    }

    public static boolean dialogWasCompletedSuccessfully(
            Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates, Long chatId) {

        return userStates.containsKey(chatId)
                && userStates.get(chatId).getKey() == COMPLETED
                && userStates.get(chatId).getValue().size() == MESSAGE_COUNT_IF_COMPLETED;
    }

    public static boolean isTheStartMessage(String message) {
        return message.equals(START_DIALOG_COMMAND);
    }

    public static boolean isSkipMessage(String message) {
        return Objects.equals(message, SKIP_THE_SETTING);
    }

    public static boolean isRestartMessage(String message) {
        return Objects.equals(message, RESTART_MESSAGE);
    }

    public static boolean isAvailableToReset(
            Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates, Long chatId) {
        return userStates.containsKey(chatId);
    }

    public static boolean isAvailableToSkip(
            Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates, Long chatId) {
        if (!userStates.containsKey(chatId)) {
            return false;
        }
        TrackMessageState currentState = userStates.get(chatId).getKey();
        return currentState == WAITING_FOR_TAGS || currentState == WAITING_FOR_FILTERS;
    }

    private static final int MESSAGE_COUNT_IF_COMPLETED = 3;
}

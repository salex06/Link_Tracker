package backend.academy.crawler.impl;

import static backend.academy.crawler.impl.TrackMessageCrawler.CrawlValidator.RESTART_MESSAGE;
import static backend.academy.crawler.impl.TrackMessageCrawler.CrawlValidator.SKIP_THE_SETTING;
import static backend.academy.crawler.impl.TrackMessageCrawler.CrawlValidator.isTheStartMessage;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.COMPLETED;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_FILTERS;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_LINK;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_TAGS;

import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.MessageCrawler;
import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component("trackCrawler")
public class TrackMessageCrawler implements MessageCrawler {
    private final Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates = new HashMap<>();

    @Override
    public DialogStateDTO crawl(Update update) {
        DialogStateDTO dto = getMessageForCornerCases(update);
        // Если краевой случай, возвращаем сформированный ответ
        if (dto != null) {
            return dto;
        }

        // Получаем информацию о предыдущем сообщении
        String previousMessageText = update.message().replyToMessage().text();
        TrackMessageState previousState = TrackMessageState.fromDescription(previousMessageText);

        // Обрабатываем сообщение в соответствии с тем, на какое сообщение он ответил
        return switch (previousState) {
            case WAITING_FOR_FILTERS -> setCompleted(update);
            case WAITING_FOR_TAGS -> createWaitingForFiltersResponse(update);
            case WAITING_FOR_LINK -> createWaitingForTagsResponse(update);
            default -> createUndefinedResponse();
        };
    }

    @Override
    public AddLinkRequest terminate(Long chatId) {
        if (!CrawlValidator.dialogWasCompletedSuccessfully(userStates, chatId)) {
            return null;
        }

        List<String> messages = userStates.get(chatId).getValue();

        String url = messages.getFirst();
        List<String> tags = Arrays.stream(messages.get(1).split(" ")).toList();
        List<String> filters = Arrays.stream(messages.get(2).split(" ")).toList();

        userStates.remove(chatId);

        return new AddLinkRequest(url, tags, filters);
    }

    private DialogStateDTO getMessageForCornerCases(Update update) {
        Message lastMessage = update.message();

        // Если введены специальные команды "Пропустить" или "Начать заново"
        if (CrawlValidator.isRestartMessage(lastMessage.text())) {
            return doRestart(update);
        } else if (CrawlValidator.isSkipMessage(lastMessage.text())) {
            return doSkipTheSetting(update);
        }

        // Если это начало диалога
        if (isTheStartMessage(lastMessage.text())) {
            // устанавливаем состояние диалога: "Ожидание ссылки"
            return createWaitingForLinkResponse(update);
        }

        // Если состояние диалога не найдено
        if (CrawlValidator.dialogStateWasNotSetYet(
                userStates, update.message().chat().id())) {
            // возвращаем пустое сообщение
            return createUndefinedResponse();
        }

        Message replyToMessage = lastMessage.replyToMessage();
        // Если сообщение отправлено без ответа
        if (replyToMessage == null) {
            // возвращаем пустое сообщение
            return createUndefinedResponse();
        }

        // Если пользователь ответил на своё сообщение
        if (!replyToMessage.from().isBot()) {
            // возвращаем ошибку
            return createErrorResponse(update, "Ошибка. Вы должны отвечать на сообщения бота");
        }

        return null;
    }

    private DialogStateDTO doRestart(Update update) {
        Long chatId = update.message().chat().id();

        if (CrawlValidator.isAvailableToReset(userStates, chatId)) {
            userStates.remove(chatId);
            return new DialogStateDTO(
                    new SendMessage(chatId, "Режим добавления ресурса для отслеживания прекращен"), false);
        }

        return createErrorResponse(update, "Ошибка. Нечего сбрасывать");
    }

    private DialogStateDTO doSkipTheSetting(Update update) {
        Long chatId = update.message().chat().id();

        if (!CrawlValidator.isAvailableToSkip(userStates, chatId)) {
            return createErrorResponse(update, "Ошибка. Нечего пропускать");
        }

        Map.Entry<TrackMessageState, List<String>> current = userStates.get(chatId);
        TrackMessageState currentState = current.getKey();
        List<String> messages = current.getValue();

        messages.add(" ");
        SendMessage message;
        if (currentState == WAITING_FOR_TAGS) {
            currentState = WAITING_FOR_FILTERS;
            message = new SendMessage(chatId, WAITING_FOR_FILTERS.description);
        } else {
            currentState = COMPLETED;
            message = null;
        }

        userStates.put(chatId, Map.entry(currentState, messages));
        return new DialogStateDTO(message, currentState == COMPLETED);
    }

    private DialogStateDTO createErrorResponse(Update update, String errorMessage) {
        Long chatId = update.message().chat().id();

        return new DialogStateDTO(new SendMessage(chatId, errorMessage), false);
    }

    private DialogStateDTO setCompleted(Update update) {
        Long chatId = update.message().chat().id();

        Map.Entry<TrackMessageState, List<String>> data = userStates.get(chatId);
        List<String> messages = userStates.get(chatId).getValue();

        if (data.getKey() != WAITING_FOR_FILTERS) {
            return createErrorResponse(update, "Ошибка. Попробуйте снова");
        }
        if (!CrawlValidator.filtersAreValid(update.message().text())) {
            return createErrorResponse(update, "Ошибка. Формат ввода фильтров: 'filter1:prop1 filter2:prop2 ...'");
        }

        messages.add(update.message().text());
        userStates.put(chatId, Map.entry(COMPLETED, messages));

        return new DialogStateDTO(null, true);
    }

    private DialogStateDTO createWaitingForLinkResponse(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        userStates.put(chatId, Map.entry(WAITING_FOR_LINK, new ArrayList<>()));

        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_LINK.description)
                        .replyToMessageId(lastMessage.messageId())
                        .replyMarkup(new ReplyKeyboardMarkup(RESTART_MESSAGE)
                                .resizeKeyboard(true)
                                .selective(true)
                                .oneTimeKeyboard(true)),
                false);
    }

    private DialogStateDTO createUndefinedResponse() {
        return new DialogStateDTO(null, false);
    }

    private DialogStateDTO createWaitingForTagsResponse(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        Map.Entry<TrackMessageState, List<String>> currentState = userStates.get(chatId);
        if (currentState.getKey() != WAITING_FOR_LINK) {
            return createErrorResponse(update, "Ошибка. Попробуйте снова");
        }

        List<String> messages = currentState.getValue();
        messages.add(lastMessage.text());
        userStates.put(chatId, Map.entry(WAITING_FOR_TAGS, messages));

        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_TAGS.description)
                        .replyToMessageId(lastMessage.messageId())
                        .replyMarkup(new ReplyKeyboardMarkup(RESTART_MESSAGE, SKIP_THE_SETTING)
                                .resizeKeyboard(true)
                                .selective(true)
                                .oneTimeKeyboard(true)),
                false);
    }

    private DialogStateDTO createWaitingForFiltersResponse(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        Map.Entry<TrackMessageState, List<String>> currentState = userStates.get(chatId);
        if (currentState.getKey() != WAITING_FOR_TAGS) {
            return createErrorResponse(update, "Ошибка. Необходимо ввести теги. Попробуйте снова");
        }

        List<String> messages = currentState.getValue();
        messages.add(lastMessage.text());
        userStates.put(chatId, Map.entry(WAITING_FOR_FILTERS, messages));

        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_FILTERS.description)
                        .replyToMessageId(lastMessage.messageId())
                        .replyMarkup(new ReplyKeyboardMarkup(RESTART_MESSAGE, SKIP_THE_SETTING)
                                .resizeKeyboard(true)
                                .selective(true)
                                .oneTimeKeyboard(true)),
                false);
    }

    @Getter
    public enum TrackMessageState {
        ERROR(null),
        UNDEFINED(null),
        WAITING_FOR_LINK("Введите ссылку:"),
        WAITING_FOR_TAGS("Введите теги (опционально):"),
        WAITING_FOR_FILTERS("Введите фильтры (опционально):"),
        COMPLETED(null);

        private String description;

        TrackMessageState(String description) {
            this.description = description;
        }

        private static final Map<String, TrackMessageState> descriptionToEnum = new HashMap<>();

        static {
            for (TrackMessageState state : values()) {
                descriptionToEnum.put(state.getDescription(), state);
            }
        }

        public static TrackMessageState fromDescription(String description) {
            return descriptionToEnum.getOrDefault(description, UNDEFINED);
        }
    }

    public static class CrawlValidator {
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
}

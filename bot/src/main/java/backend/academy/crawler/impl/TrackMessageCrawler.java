package backend.academy.crawler.impl;

import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.COMPLETED;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.ERROR;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.UNDEFINED;
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

/** Класс предоставляет возможность обрабатывать составную команду регистрации ресурса в качестве отслеживаемого. */
public class TrackMessageCrawler implements MessageCrawler {
    private static final String START_DIALOG_COMMAND = "/track";
    private static final String RESTART_MESSAGE = "Сбросить";
    private static final String SKIP_THE_SETTING = "Пропустить";
    private static final int MESSAGE_COUNT_IF_COMPLETED = 3;
    private final Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates = new HashMap<>();

    @Override
    public DialogStateDTO crawl(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        // Если введены специальные команды "Пропустить" или "Начать заново"
        if (Objects.equals(lastMessage.text(), RESTART_MESSAGE)) {
            return doRestart(update);
        } else if (Objects.equals(lastMessage.text(), SKIP_THE_SETTING)) {
            return doSkipTheSetting(update);
        }

        // Если это начало диалога
        if (isTheStartMessage(update)) {
            // устанавливаем состояние диалога: "Ожидание ссылки"
            changeStateToWaitingForLink(chatId);
            return createWaitingForLinkResponse(update);
        }

        // Если состояние диалога не найдено
        if (dialogStateWasNotSetYet(update.message().chat().id())) {
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

        // Получаем инфомрацию о предыдущем сообщении
        String previousMessageText = replyToMessage.text();
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
        if (!dialogWasCompletedSuccessfully(chatId)) {
            return null;
        }

        List<String> messages = userStates.get(chatId).getValue();

        String url = messages.getFirst();
        List<String> tags = Arrays.stream(messages.get(1).split(" ")).toList();
        List<String> filters = Arrays.stream(messages.get(2).split(" ")).toList();

        userStates.remove(chatId);

        return new AddLinkRequest(url, tags, filters);
    }

    private boolean dialogWasCompletedSuccessfully(Long chatId) {
        return userStates.containsKey(chatId)
                && userStates.get(chatId).getKey() == COMPLETED
                && userStates.get(chatId).getValue().size() == MESSAGE_COUNT_IF_COMPLETED;
    }

    private DialogStateDTO doRestart(Update update) {
        Long chatId = update.message().chat().id();
        if (userStates.containsKey(chatId)) {
            userStates.remove(chatId);
            return new DialogStateDTO(
                    new SendMessage(chatId, "Режим добавления ресурса для отслеживания прекращен"), UNDEFINED);
        }
        return createErrorResponse(update, "Ошибка. Нечего сбрасывать");
    }

    private DialogStateDTO doSkipTheSetting(Update update) {
        Long chatId = update.message().chat().id();

        if (!userStates.containsKey(chatId)) {
            return createErrorResponse(update, "Ошибка. Нечего пропускать");
        }

        Map.Entry<TrackMessageState, List<String>> current = userStates.get(chatId);
        TrackMessageState currentState = current.getKey();
        List<String> messages = current.getValue();

        if (currentState != WAITING_FOR_TAGS && currentState != WAITING_FOR_FILTERS) {
            return createErrorResponse(update, "Можно пропустить только выбор тегов и фильтров!");
        }

        messages.add("");
        SendMessage message;
        if (currentState == WAITING_FOR_TAGS) {
            currentState = WAITING_FOR_FILTERS;
            message = new SendMessage(chatId, WAITING_FOR_FILTERS.description);
        } else {
            currentState = COMPLETED;
            message = null;
        }
        userStates.put(chatId, Map.entry(currentState, messages));
        return new DialogStateDTO(message, currentState);
    }

    private boolean isTheStartMessage(Update update) {
        String message = update.message().text();

        return message.equals(START_DIALOG_COMMAND);
    }

    private void changeStateToWaitingForLink(Long chatId) {
        userStates.put(chatId, Map.entry(WAITING_FOR_LINK, new ArrayList<>()));
    }

    private boolean dialogStateWasNotSetYet(Long chatId) {
        return !userStates.containsKey(chatId);
    }

    private DialogStateDTO createErrorResponse(Update update, String errorMessage) {
        Long chatId = update.message().chat().id();

        return new DialogStateDTO(new SendMessage(chatId, errorMessage), ERROR);
    }

    private DialogStateDTO setCompleted(Update update) {
        Long chatId = update.message().chat().id();

        Map.Entry<TrackMessageState, List<String>> data = userStates.get(chatId);
        List<String> messages = userStates.get(chatId).getValue();
        if (data.getKey() != WAITING_FOR_FILTERS) {
            return createErrorResponse(update, "Ошибка. Попробуйте снова");
        }

        if (!filtersAreValid(update.message().text())) {
            return createErrorResponse(update, "Ошибка. Формат ввода фильтров: 'filter1:prop1 filter2:prop2 ...'");
        }

        messages.add(update.message().text());
        userStates.put(chatId, Map.entry(COMPLETED, messages));

        return new DialogStateDTO(null, COMPLETED);
    }

    private boolean filtersAreValid(String text) {
        return text.matches("^(\\w+:\\w+)( \\w+:\\w+)*$");
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
                WAITING_FOR_LINK);
    }

    private DialogStateDTO createUndefinedResponse() {
        return new DialogStateDTO(null, UNDEFINED);
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
                WAITING_FOR_TAGS);
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
                WAITING_FOR_FILTERS);
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
                descriptionToEnum.put(state.description(), state);
            }
        }

        public static TrackMessageState fromDescription(String description) {
            return descriptionToEnum.getOrDefault(description, UNDEFINED);
        }
    }
}

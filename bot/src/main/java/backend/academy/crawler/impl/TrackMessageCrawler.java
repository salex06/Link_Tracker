package backend.academy.crawler.impl;

import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.COMPLETED;
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

public class TrackMessageCrawler implements MessageCrawler {
    private static final String RESTART_MESSAGE = "Начать заново";
    private static final String SKIP_THE_SETTING = "Пропустить";
    private static final int MESSAGE_COUNT_IF_COMPLETED = 3;
    private final Map<Long, Map.Entry<TrackMessageState, List<String>>> userStates = new HashMap<>();

    @Override
    public DialogStateDTO crawl(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();
        userStates.computeIfAbsent(chatId, aLong -> Map.entry(WAITING_FOR_LINK, new ArrayList<>()));

        if (Objects.equals(lastMessage.text(), RESTART_MESSAGE)) {
            return doRestart(chatId);
        } else if (Objects.equals(lastMessage.text(), SKIP_THE_SETTING)) {
            return doSkipTheSetting(chatId);
        }

        Message reply = lastMessage.replyToMessage();
        if (reply == null) {
            if (Objects.equals(lastMessage.text(), "/track")) {
                return createWaitingForLinkResponse(update);
            }
            return createUndefinedResponse();
        }

        DialogStateDTO response = createUndefinedResponse();

        String previousMessageText = reply.text();
        TrackMessageState previousState = TrackMessageState.fromDescription(previousMessageText);
        switch (previousState) {
            case WAITING_FOR_FILTERS:
                response = setCompleted(update);
                break;
            case WAITING_FOR_TAGS:
                response = createWaitingForFiltersResponse(update);
                break;
            case WAITING_FOR_LINK:
                response = createWaitingForTagsResponse(update);
                break;
            default:
                if (Objects.equals(lastMessage.text(), "/track")) {
                    response = createWaitingForLinkResponse(update);
                }
                break;
        }
        return response;
    }

    private DialogStateDTO doRestart(Long chatId) {
        userStates.remove(chatId);
        return new DialogStateDTO(
                new SendMessage(chatId, "Режим добавления ресурса для отслеживания прекращен"), UNDEFINED);
    }

    private DialogStateDTO doSkipTheSetting(Long chatId) {
        Map.Entry<TrackMessageState, List<String>> current = userStates.get(chatId);
        TrackMessageState currentState = current.getKey();
        List<String> messages = current.getValue();
        if (currentState != WAITING_FOR_TAGS && currentState != WAITING_FOR_FILTERS) {
            return new DialogStateDTO(new SendMessage(chatId, "Нельзя пропустить!"), UNDEFINED);
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
        return !userStates.containsKey(chatId)
                || userStates.get(chatId).getKey() != COMPLETED
                || userStates.get(chatId).getValue().size() == MESSAGE_COUNT_IF_COMPLETED;
    }

    private DialogStateDTO setCompleted(Update update) {
        Long chatId = update.message().chat().id();

        Map.Entry<TrackMessageState, List<String>> data = userStates.get(chatId);
        List<String> messages = userStates.get(chatId).getValue();
        if (data.getKey() != WAITING_FOR_FILTERS || messages.size() != MESSAGE_COUNT_IF_COMPLETED - 1) {
            return createUndefinedResponse();
        }

        messages.add(update.message().text());
        userStates.put(chatId, Map.entry(COMPLETED, messages));

        return new DialogStateDTO(null, COMPLETED);
    }

    private DialogStateDTO createWaitingForLinkResponse(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_LINK.description)
                        .replyToMessageId(lastMessage.messageId())
                        .replyMarkup(new ReplyKeyboardMarkup("Начать заново")
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
            return createUndefinedResponse();
        }

        List<String> messages = currentState.getValue();
        messages.add(lastMessage.text());
        userStates.put(chatId, Map.entry(WAITING_FOR_TAGS, messages));

        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_TAGS.description)
                        .replyToMessageId(lastMessage.messageId())
                        .replyMarkup(new ReplyKeyboardMarkup("Начать заново", "Пропустить")
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
            return createUndefinedResponse();
        }

        List<String> messages = currentState.getValue();
        messages.add(lastMessage.text());
        userStates.put(chatId, Map.entry(WAITING_FOR_FILTERS, messages));

        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_FILTERS.description)
                        .replyToMessageId(lastMessage.messageId())
                        .replyMarkup(new ReplyKeyboardMarkup("Начать заново", "Пропустить")
                                .resizeKeyboard(true)
                                .selective(true)
                                .oneTimeKeyboard(true)),
                WAITING_FOR_FILTERS);
    }

    @Getter
    public enum TrackMessageState {
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

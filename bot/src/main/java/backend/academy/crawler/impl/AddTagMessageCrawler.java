package backend.academy.crawler.impl;

import static backend.academy.crawler.impl.AddTagMessageCrawler.AddTagMessageState.WAITING_FOR_LINK;
import static backend.academy.crawler.impl.AddTagMessageCrawler.CrawlValidator.*;

import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.MessageCrawler;
import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component("addTagCrawler")
public class AddTagMessageCrawler implements MessageCrawler {
    private final Map<Long, Map.Entry<Boolean, List<String>>> userStates = new HashMap<>();

    @Override
    public DialogStateDTO crawl(Update update) {
        DialogStateDTO dto = getMessageForCornerCases(update);
        // Если краевой случай, возвращаем сформированный ответ
        if (dto != null) {
            return dto;
        }

        String previousMessageText = update.message().replyToMessage().text();
        AddTagMessageState previousState = AddTagMessageState.fromDescription(previousMessageText);

        if (previousState == WAITING_FOR_LINK) {
            return setCompleted(update);
        }
        return createUndefinedResponse();
    }

    @Override
    public AddLinkRequest terminate(Long id) {
        if (!dialogWasCompletedSuccessfully(userStates, id)) {
            return null;
        }

        List<String> messages = userStates.get(id).getValue();

        String tag = messages.get(0);
        String url = messages.get(1);

        userStates.remove(id);

        return new AddLinkRequest(url, List.of(tag), List.of());
    }

    private DialogStateDTO getMessageForCornerCases(Update update) {
        Message lastMessage = update.message();

        // Если это начало диалога
        if (isStartMessage(lastMessage.text().split(" ", 2)[0])) {
            // устанавливаем состояние диалога: "Ожидание ссылки"
            return createWaitingForLinkResponse(update);
        }

        // Если состояние диалога не найдено
        if (dialogStateWasNotSetYet(userStates, update.message().chat().id())) {
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

    private DialogStateDTO createErrorResponse(Update update, String errorMessage) {
        Long chatId = update.message().chat().id();

        return new DialogStateDTO(new SendMessage(chatId, errorMessage), false);
    }

    private DialogStateDTO createWaitingForLinkResponse(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        if (!isCorrectStartMessage(lastMessage.text())) {
            return new DialogStateDTO(new SendMessage(chatId, "Ошибка ввода! Попробуйте снова"), false);
        }

        userStates.put(chatId, Map.entry(false, new ArrayList<>()));

        String[] splittedMessage = lastMessage.text().split(" ", 2);

        userStates.get(chatId).getValue().add(splittedMessage[1]);
        return new DialogStateDTO(
                new SendMessage(chatId, WAITING_FOR_LINK.description).replyToMessageId(lastMessage.messageId()), false);
    }

    private DialogStateDTO setCompleted(Update update) {
        Long chatId = update.message().chat().id();

        if (!userStates.containsKey(chatId)) {
            return createErrorResponse(update, "Ошибка. Попробуйте снова");
        }

        Map.Entry<Boolean, List<String>> data = userStates.get(chatId);
        List<String> messages = data.getValue();

        messages.add(update.message().text());
        userStates.put(chatId, Map.entry(true, messages));

        return new DialogStateDTO(null, true);
    }

    private DialogStateDTO createUndefinedResponse() {
        return new DialogStateDTO(null, false);
    }

    @Getter
    public enum AddTagMessageState {
        ERROR(null),
        UNDEFINED(null),
        WAITING_FOR_LINK("Введите ссылку для тега (ALL - добавить тег для всех ссылок):"),
        COMPLETED(null);

        private String description;

        AddTagMessageState(String description) {
            this.description = description;
        }

        private static final Map<String, AddTagMessageState> descriptionToEnum = new HashMap<>();

        static {
            for (AddTagMessageState state : values()) {
                descriptionToEnum.put(state.description(), state);
            }
        }

        public static AddTagMessageState fromDescription(String description) {
            return descriptionToEnum.getOrDefault(description, UNDEFINED);
        }
    }

    public static class CrawlValidator {
        public static final String START_DIALOG_COMMAND = "/addtag";

        private CrawlValidator() {}

        public static boolean isStartMessage(String message) {
            return message.equals(START_DIALOG_COMMAND);
        }

        public static boolean dialogStateWasNotSetYet(Map<Long, Map.Entry<Boolean, List<String>>> userStates, Long id) {
            return !userStates.containsKey(id);
        }

        public static boolean isCorrectStartMessage(String message) {
            return message.matches("^/addtag [a-zA-Z0-9-_]+$");
        }

        public static boolean dialogWasCompletedSuccessfully(
                Map<Long, Map.Entry<Boolean, List<String>>> userStates, Long id) {
            return userStates.containsKey(id)
                    && userStates.get(id).getKey()
                    && userStates.get(id).getValue().size() == MESSAGE_COUNT_IF_COMPLETED;
        }

        private static final int MESSAGE_COUNT_IF_COMPLETED = 2;
    }
}

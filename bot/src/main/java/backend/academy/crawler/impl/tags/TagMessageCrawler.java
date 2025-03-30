package backend.academy.crawler.impl.tags;

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
import org.springframework.beans.factory.annotation.Autowired;

public class TagMessageCrawler implements MessageCrawler {
    protected final Map<Long, Map.Entry<Boolean, List<String>>> userStates = new HashMap<>();
    protected final TagValidator tagValidator;

    @Autowired
    protected TagMessageCrawler(TagValidator tagValidator) {
        this.tagValidator = tagValidator;
    }

    @Override
    public DialogStateDTO crawl(Update update) {
        DialogStateDTO dto = getMessageForCornerCases(update);
        // Если краевой случай, возвращаем сформированный ответ
        if (dto != null) {
            return dto;
        }

        String previousMessageText = update.message().replyToMessage().text();
        TagMessageState previousState = TagMessageState.fromDescription(previousMessageText);

        if (previousState == TagMessageState.WAITING_FOR_LINK) {
            return setCompleted(update);
        }
        return createUndefinedResponse();
    }

    @Override
    public AddLinkRequest terminate(Long id) {
        if (!tagValidator.dialogWasCompletedSuccessfully(userStates, id)) {
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
        if (tagValidator.isStartMessage(lastMessage.text().split(" ", 2)[0])) {
            // устанавливаем состояние диалога: "Ожидание ссылки"
            return createWaitingForLinkResponse(update);
        }

        // Если состояние диалога не найдено
        if (tagValidator.dialogStateWasNotSetYet(
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

    private DialogStateDTO createErrorResponse(Update update, String errorMessage) {
        Long chatId = update.message().chat().id();

        return new DialogStateDTO(new SendMessage(chatId, errorMessage), false);
    }

    private DialogStateDTO createWaitingForLinkResponse(Update update) {
        Long chatId = update.message().chat().id();
        Message lastMessage = update.message();

        if (!tagValidator.isCorrectStartMessage(lastMessage.text())) {
            return new DialogStateDTO(new SendMessage(chatId, "Ошибка ввода! Попробуйте снова"), false);
        }

        userStates.put(chatId, Map.entry(false, new ArrayList<>()));

        String[] splittedMessage = lastMessage.text().split(" ", 2);

        userStates.get(chatId).getValue().add(splittedMessage[1]);
        return new DialogStateDTO(
                new SendMessage(chatId, TagMessageState.WAITING_FOR_LINK.description)
                        .replyToMessageId(lastMessage.messageId()),
                false);
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
    public enum TagMessageState {
        ERROR(null),
        UNDEFINED(null),
        WAITING_FOR_LINK("Введите ссылку для тега (ALL - применить для всех ссылок):"),
        COMPLETED(null);

        private String description;

        TagMessageState(String description) {
            this.description = description;
        }

        private static final Map<String, TagMessageState> descriptionToEnum = new HashMap<>();

        static {
            for (TagMessageState state : values()) {
                descriptionToEnum.put(state.description(), state);
            }
        }

        public static TagMessageState fromDescription(String description) {
            return descriptionToEnum.getOrDefault(description, UNDEFINED);
        }
    }
}

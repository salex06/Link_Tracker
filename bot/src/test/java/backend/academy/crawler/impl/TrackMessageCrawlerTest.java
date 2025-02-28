package backend.academy.crawler.impl;

import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.COMPLETED;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.ERROR;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.UNDEFINED;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_FILTERS;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_LINK;
import static backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState.WAITING_FOR_TAGS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.impl.TrackMessageCrawler.TrackMessageState;
import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TrackMessageCrawlerTest {
    private TrackMessageCrawler trackMessageCrawler;

    @BeforeEach
    void setUp() {
        trackMessageCrawler = new TrackMessageCrawler();
    }

    @Test
    public void crawl_WhenRestartAndAnyStateIsSet_ThenReturnSuccessMessage() {
        setWaitingForLinkState();
        String expectedMessage = "Режим добавления ресурса для отслеживания прекращен";
        TrackMessageState expectedState = UNDEFINED;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("Сбросить");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenRestartAndNoStateIsSet_ThenReturnError() {
        String expectedMessage = "Ошибка. Нечего сбрасывать";
        TrackMessageState expectedState = ERROR;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("Сбросить");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenSkipCommandAndWaitingForTagsStateIsSet_ThenReturnSuccess() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        String expectedMessage = "Введите фильтры (опционально):";
        TrackMessageState expectedState = WAITING_FOR_FILTERS;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("Пропустить");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenSkipCommandAndWrongState_ThenReturnError() {
        String expectedMessage = "Ошибка. Нечего пропускать";
        TrackMessageState expectedState = ERROR;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("Пропустить");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenSkipCommandAndWaitingForFiltersStateIsSet_ThenReturnSuccess() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        setWaitingForFiltersState();
        TrackMessageState expectedState = COMPLETED;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("Пропустить");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenTrackCommandWasEntered_ThenReturnWaitingForLinkResponse() {
        String expectedMessage = "Введите ссылку:";
        TrackMessageState expectedState = WAITING_FOR_LINK;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/track");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenTrackStateNotFound_ThenReturnUndefined() {
        TrackMessageState expectedState = UNDEFINED;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/any");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenReplyToMessageIsNull_ThenReturnUndefined() {
        String expectedMessage = null;
        TrackMessageState expectedState = UNDEFINED;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/any");
        when(message.replyToMessage()).thenReturn(null);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenReplyToYourself_ThenReturnError() {
        setWaitingForLinkState();
        String expectedMessage = "Ошибка. Вы должны отвечать на сообщения бота";
        TrackMessageState expectedState = ERROR;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/any");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(false);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForLinkAndCorrectStateInStorage_ThenReturnSuccess() {
        setWaitingForLinkState();
        String expectedMessage = "Введите теги (опционально):";
        TrackMessageState expectedState = WAITING_FOR_TAGS;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("myLink");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите ссылку:");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForLinkAndWrongStateInStorage_ThenReturnError() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        String expectedMessage = "Ошибка. Попробуйте снова";
        TrackMessageState expectedState = ERROR;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("myLink");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите ссылку:");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForTagsAndCorrectStateInStorage_ThenReturnSuccess() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        String expectedMessage = "Введите фильтры (опционально):";
        TrackMessageState expectedState = WAITING_FOR_FILTERS;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("tag1 tag2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите теги (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForTagsAndWrongStateInStorage_ThenReturnError() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        setWaitingForFiltersState();
        String expectedMessage = "Ошибка. Необходимо ввести теги. Попробуйте снова";
        TrackMessageState expectedState = ERROR;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("tag1 tag2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите теги (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForFiltersAndCorrectStateInStorage_ThenReturnSuccess() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        setWaitingForFiltersState();
        TrackMessageState expectedState = COMPLETED;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("filter1:prop1 filter2:prop2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите фильтры (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForFiltersAndWrongStateInStorage_ThenReturnError() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        String expectedMessage = "Ошибка. Попробуйте снова";
        TrackMessageState expectedState = ERROR;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("filter1:prop1 filter2:prop2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите фильтры (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = trackMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.state());
    }

    @Test
    public void terminate_WhenDialogWasCompletedSuccessfully_ThenReturnSuccess() {
        String expectedURL = "myLink";
        List<String> expectedTags = List.of("tag1", "tag2");
        List<String> expectedFilters = List.of("filter1:prop1", "filter2:prop2");
        setWaitingForLinkState();
        setWaitingForTagsState();
        setWaitingForFiltersState();
        setCompleted();
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("filter1:prop1 filter2:prop2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите фильтры (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        AddLinkRequest actual = trackMessageCrawler.terminate(chat.id());

        assertEquals(expectedURL, actual.url());
        assertEquals(expectedTags, actual.tags());
        assertEquals(expectedFilters, actual.filters());
    }

    @Test
    public void terminate_WhenDialogWasNotCompleted_ThenReturnError() {
        setWaitingForLinkState();
        setWaitingForTagsState();
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("filter1:prop1 filter2:prop2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите фильтры (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        AddLinkRequest actual = trackMessageCrawler.terminate(chat.id());

        assertNull(actual);
    }

    private void setWaitingForLinkState() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/track");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        trackMessageCrawler.crawl(update);
    }

    private void setWaitingForTagsState() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("myLink");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите ссылку:");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);
        trackMessageCrawler.crawl(update);
    }

    public void setWaitingForFiltersState() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("tag1 tag2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите теги (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);
        trackMessageCrawler.crawl(update);
    }

    public void setCompleted() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("filter1:prop1 filter2:prop2");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите фильтры (опционально):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);
        trackMessageCrawler.crawl(update);
    }
}

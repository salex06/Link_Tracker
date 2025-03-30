package backend.academy.crawler.impl;

import backend.academy.crawler.DialogStateDTO;
import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AddTagMessageCrawlerTest {
    private AddTagMessageCrawler addTagMessageCrawler;

    @BeforeEach
    void setUp() {
        addTagMessageCrawler = new AddTagMessageCrawler();
    }

    @Test
    public void test_WhenIsStartMessage_ThenCreateWaitingForLinkResponse() {
        String expectedMessage = "Введите ссылку для тега (ALL - добавить тег для всех ссылок):";
        Boolean expectedState = false;
        String tagName = "myTag";
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/addtag " + tagName);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.isCompleted());
    }

    @Test
    public void test_WhenDialogStateWasNotSetYet_ThenCreateUndefinedRespones() {
        Boolean expectedState = false;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/any");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.isCompleted());
    }

    @Test
    public void crawl_WhenReplyToMessageIsNull_ThenReturnUndefined() {
        try (MockedStatic<AddTagMessageCrawler.CrawlValidator> validator = Mockito.mockStatic(AddTagMessageCrawler.CrawlValidator.class)) {
            String expectedMessage = null;
            Boolean expectedState = false;
            Update update = Mockito.mock(Update.class);
            Message message = Mockito.mock(Message.class);
            Chat chat = Mockito.mock(Chat.class);
            when(update.message()).thenReturn(message);
            validator.when(() -> AddTagMessageCrawler.CrawlValidator.dialogStateWasNotSetYet(anyMap(), anyLong())).thenReturn(false);

            when(message.text()).thenReturn("ALL");
            when(message.replyToMessage()).thenReturn(null);
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);

            DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

            assertNull(actualState.message());
            assertEquals(expectedState, actualState.isCompleted());
        }
    }

    @Test
    public void crawl_WhenReplyToYourself_ThenReturnError() {
        try (MockedStatic<AddTagMessageCrawler.CrawlValidator> validator = Mockito.mockStatic(AddTagMessageCrawler.CrawlValidator.class)) {
            String expectedMessage = "Ошибка. Вы должны отвечать на сообщения бота";
            Boolean expectedState = false;
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
            validator.when(() -> AddTagMessageCrawler.CrawlValidator.dialogStateWasNotSetYet(anyMap(), anyLong())).thenReturn(false);

            DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

            assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
            assertEquals(expectedState, actualState.isCompleted());
        }
    }

    @Test
    public void crawl_WhenPreviousStateIsWaitingForLinkAndCorrectStateInStorage_ThenReturnSuccess() {
        setWaitingForLinkState();
        String expectedMessage = null;
        Boolean expectedState = true;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("ALL");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - добавить тег для всех ссылок):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.isCompleted());
    }

    @Test
    public void terminate_WhenDialogWasNotCompleted_ThenReturnError() {
        try (MockedStatic<AddTagMessageCrawler.CrawlValidator> validator = Mockito.mockStatic(AddTagMessageCrawler.CrawlValidator.class)) {
            Update update = Mockito.mock(Update.class);
            Message message = Mockito.mock(Message.class);
            Chat chat = Mockito.mock(Chat.class);
            Message replyToMessage = Mockito.mock(Message.class);
            User user = Mockito.mock(User.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("ALL");
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);
            when(message.replyToMessage()).thenReturn(replyToMessage);
            when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - добавить тег для всех ссылок):");
            when(message.from()).thenReturn(user);
            when(replyToMessage.from()).thenReturn(user);
            when(user.isBot()).thenReturn(true);
            validator.when(() -> AddTagMessageCrawler.CrawlValidator.dialogWasCompletedSuccessfully(anyMap(), anyLong())).thenReturn(false);

            AddLinkRequest actual = addTagMessageCrawler.terminate(chat.id());

            assertNull(actual);
        }
    }

    @Test
    public void terminate_WhenDialogWasCompleted_ThenReturnSuccess() {
        try (MockedStatic<AddTagMessageCrawler.CrawlValidator> validator = Mockito.mockStatic(AddTagMessageCrawler.CrawlValidator.class)) {
            validator.when(() -> AddTagMessageCrawler.CrawlValidator.isStartMessage(anyString())).thenCallRealMethod();
            validator.when(() -> AddTagMessageCrawler.CrawlValidator.isCorrectStartMessage(anyString())).thenCallRealMethod();
            setWaitingForLinkState();
            setCompleted();
            List<String> expectedTag = List.of("myTag");
            Update update = Mockito.mock(Update.class);
            Message message = Mockito.mock(Message.class);
            Chat chat = Mockito.mock(Chat.class);
            Message replyToMessage = Mockito.mock(Message.class);
            User user = Mockito.mock(User.class);
            when(update.message()).thenReturn(message);
            when(message.text()).thenReturn("ALL");
            when(message.chat()).thenReturn(chat);
            when(chat.id()).thenReturn(1L);
            when(message.replyToMessage()).thenReturn(replyToMessage);
            when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - добавить тег для всех ссылок):");
            when(message.from()).thenReturn(user);
            when(replyToMessage.from()).thenReturn(user);
            when(user.isBot()).thenReturn(true);
            validator.when(() -> AddTagMessageCrawler.CrawlValidator.dialogWasCompletedSuccessfully(anyMap(), anyLong())).thenReturn(true);

            AddLinkRequest actual = addTagMessageCrawler.terminate(chat.id());

            assertNotNull(actual);
            assertEquals(message.text(), actual.link());
            assertEquals(expectedTag, actual.tags());
        }
    }

    private void setWaitingForLinkState() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("/addtag myTag");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        addTagMessageCrawler.crawl(update);
    }

    public void setCompleted() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        Message replyToMessage = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn("ALL");
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
        when(message.replyToMessage()).thenReturn(replyToMessage);
        when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - добавить тег для всех ссылок):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);
        addTagMessageCrawler.crawl(update);
    }
}

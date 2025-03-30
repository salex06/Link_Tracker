package backend.academy.crawler.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import backend.academy.crawler.DialogStateDTO;
import backend.academy.crawler.impl.tags.add.AddTagMessageCrawler;
import backend.academy.crawler.impl.tags.add.AddTagValidator;
import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AddTagMessageCrawlerTest {
    private AddTagMessageCrawler addTagMessageCrawler;
    private AddTagValidator addTagValidator;

    @BeforeEach
    void setUp() {
        addTagValidator = new AddTagValidator();
        addTagMessageCrawler = new AddTagMessageCrawler(addTagValidator);
    }

    @Test
    public void test_WhenIsStartMessage_ThenCreateWaitingForLinkResponse() {
        String expectedMessage = "Введите ссылку для тега (ALL - применить для всех ссылок):";
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
    public void test_WhenDialogStateWasNotSetYet_ThenCreateUndefinedResponse() {
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
        String expectedMessage = null;
        Boolean expectedState = false;
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        Chat chat = Mockito.mock(Chat.class);
        when(update.message()).thenReturn(message);

        when(message.text()).thenReturn("ALL");
        when(message.replyToMessage()).thenReturn(null);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);

        DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.isCompleted());
    }

    @Test
    public void crawl_WhenReplyToYourself_ThenReturnError() {
        setWaitingForLinkState();
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

        DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

        assertEquals(expectedMessage, actualState.message().getParameters().get("text"));
        assertEquals(expectedState, actualState.isCompleted());
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
        when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - применить для всех ссылок):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        DialogStateDTO actualState = addTagMessageCrawler.crawl(update);

        assertNull(actualState.message());
        assertEquals(expectedState, actualState.isCompleted());
    }

    @Test
    public void terminate_WhenDialogWasNotCompleted_ThenReturnError() {
        setWaitingForLinkState();
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
        when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - применить для всех ссылок):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        AddLinkRequest actual = addTagMessageCrawler.terminate(chat.id());

        assertNull(actual);
    }

    @Test
    public void terminate_WhenDialogWasCompleted_ThenReturnSuccess() {
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
        when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - применить для всех ссылок):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);

        AddLinkRequest actual = addTagMessageCrawler.terminate(chat.id());

        assertNotNull(actual);
        assertEquals(message.text(), actual.link());
        assertEquals(expectedTag, actual.tags());
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
        when(replyToMessage.text()).thenReturn("Введите ссылку для тега (ALL - применить для всех ссылок):");
        when(message.from()).thenReturn(user);
        when(replyToMessage.from()).thenReturn(user);
        when(user.isBot()).thenReturn(true);
        addTagMessageCrawler.crawl(update);
    }
}

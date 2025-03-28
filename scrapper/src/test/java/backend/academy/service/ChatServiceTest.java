package backend.academy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.Link;
import backend.academy.model.TgChat;
import backend.academy.repository.ChatRepository;
import backend.academy.repository.impl.MapTgChatRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatServiceTest {
    private ChatRepository chatRepository;
    private ChatService chatService;

    @BeforeEach
    void setup() {
        chatRepository = new MapTgChatRepository();
        chatService = new ChatService(chatRepository);
    }

    @Test
    public void saveChat_WhenChatHasNotBeenAddedYet_ThenReturnTrue() {
        Long chatId = 5L;

        boolean result = chatService.saveChat(chatId);

        assertThat(result).isTrue();
    }

    @Test
    public void saveChat_WhenChatHasAlreadyBeenAdded_ThenReturnFalse() {
        Long chatId = 5L;
        chatRepository.save(chatId);

        boolean result = chatService.saveChat(chatId);

        assertThat(result).isFalse();
    }

    @Test
    public void testGetAllChatReturnsCorrectValues() {
        Long chatId1 = 1L;
        Long chatId2 = 2L;
        chatRepository.save(chatId1);
        chatRepository.save(chatId2);

        List<TgChat> result = chatService.getAllChat();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(new TgChat(chatId1, new HashSet<>()), new TgChat(chatId2, new HashSet<>()));
    }

    @Test
    public void containsChat_WhenChatIsInTheDataBase_ThenReturnTrue() {
        Long chatId = 1L;
        chatRepository.save(chatId);

        boolean result = chatService.containsChat(chatId);

        assertThat(result).isTrue();
    }

    @Test
    public void containsChat_WhenChatIsNotInTheDataBase_ThenReturnFalse() {
        Long chatId = 1L;

        boolean result = chatService.containsChat(chatId);

        assertThat(result).isFalse();
    }

    @Test
    public void deleteChat_WhenChatIsInTheDataBase_ThenReturnTrue() {
        Long chatId = 1L;
        chatRepository.save(chatId);

        boolean result = chatService.deleteChat(chatId);

        assertThat(result).isTrue();
        assertThat(chatRepository.getAll()).isEmpty();
    }

    @Test
    public void deleteChat_WhenChatIsNotInTheDataBase_ThenReturnFalse() {
        Long chatId = 1L;

        boolean result = chatService.deleteChat(chatId);

        assertThat(result).isFalse();
    }

    @Test
    public void deleteLink_WhenChatIsNotInDataBase_ThenReturnFalse() {
        Long chatId = 1L;

        boolean result = chatService.deleteLink(chatId, "test");

        assertThat(result).isFalse();
    }

    @Test
    public void deleteLink_WhenLinkNotFound_ThenReturnFalse() {
        Long chatId = 1L;
        chatRepository.save(chatId);

        boolean result = chatService.deleteLink(chatId, "test");

        assertThat(result).isFalse();
    }

    @Test
    public void deleteLink_WhenChatIsInDataBaseAndLinkWasDeleted_ThenReturnTrue() {
        Link link = new Link("test");
        Long chatId = 1L;
        chatRepository.save(chatId);
        TgChat chat = chatRepository.getById(chatId).get();
        chat.addLink(link);
        chatRepository.save(chatId);

        boolean result = chatService.deleteLink(chatId, link.getUrl());

        assertThat(result).isTrue();
        assertThat(chatRepository.getAll().size()).isEqualTo(1);
        assertThat(chatRepository.getAll().getFirst()).isEqualTo(chat);
    }

    @Test
    public void getChatLinks_WhenChatNotFound_ThenReturnNull() {
        Long chatId = 1L;

        Set<Link> result = chatService.getChatLinks(chatId);

        assertNull(result);
    }

    @Test
    public void getChatLinks_WhenChatExists_ThenReturnSetOfLinks() {
        Link link1 = new Link("link1");
        Link link2 = new Link("link2");
        Long chatId = 1L;
        chatRepository.save(chatId);
        TgChat chat = chatRepository.getById(chatId).get();
        chat.addLink(link1);
        chat.addLink(link2);

        Set<Link> result = chatService.getChatLinks(chatId);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(link1, link2);
    }

    @Test
    public void appendLinkToChat_WhenChatNotFound_ThenReturnFalse() {
        Long chatId = 1L;

        boolean result = chatService.appendLinkToChat(chatId, new Link("test"));

        assertThat(result).isFalse();
    }

    @Test
    public void appendLinkToChat_WhenChatExists_ThenReturnTrue() {
        Long chatId = 1L;
        chatRepository.save(chatId);
        TgChat chat = chatRepository.getById(chatId).get();

        boolean result = chatService.appendLinkToChat(chatId, new Link("test"));

        assertThat(result).isTrue();
        assertThat(chat.links()).contains(new Link("test"));
    }
}

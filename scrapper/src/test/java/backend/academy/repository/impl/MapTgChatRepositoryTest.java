package backend.academy.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.model.Link;
import backend.academy.model.TgChat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapTgChatRepositoryTest {
    private MapTgChatRepository mapTgChatRepository;

    @BeforeEach
    public void setup() {
        mapTgChatRepository = new MapTgChatRepository();
    }

    @Test
    public void getById_WhenChatNotFound_ThenReturnEmpty() {
        Long chatId = 1L;

        Optional<TgChat> actualChat = mapTgChatRepository.getById(chatId);

        assertThat(actualChat).isEmpty();
    }

    @Test
    public void getById_WhenChatExists_ThenReturnTgChat() {
        Long chatId = 1L;
        mapTgChatRepository.save(chatId);

        Optional<TgChat> actualChat = mapTgChatRepository.getById(chatId);

        assertThat(actualChat).isNotEmpty();
        assertThat(actualChat.get().id()).isEqualTo(chatId);
    }

    @Test
    public void getAllReturnsListOfTgChats() {
        TgChat chat1 = new TgChat(1L, Set.of(new Link(1L, "chat1Link")));
        TgChat chat2 = new TgChat(2L, new HashSet<>());
        mapTgChatRepository.saveTgChat(chat1);
        mapTgChatRepository.saveTgChat(chat2);

        List<TgChat> actual = mapTgChatRepository.getAll();

        assertThat(actual).contains(chat1, chat2);
    }

    @Test
    public void save_WhenTgChatNotInDataBase_ThenSaveTgChatAndReturnTrue() {
        Long chatId = 1L;

        boolean result = mapTgChatRepository.save(chatId);

        assertThat(result).isTrue();
        assertThat(mapTgChatRepository.getById(chatId)).isNotEmpty();
    }

    @Test
    public void save_WhenTgChatIsInDataBase_ThenReturnFalse() {
        Long chatId = 1L;
        mapTgChatRepository.save(chatId);

        boolean result = mapTgChatRepository.save(chatId);

        assertThat(result).isFalse();
    }

    @Test
    public void saveTgChat_WhenTgChatNotFound_ThenSaveTgChatAndReturnNull() {
        TgChat chat = new TgChat(1L, new HashSet<>());

        TgChat result = mapTgChatRepository.saveTgChat(chat);

        assertNull(result);
    }

    @Test
    public void saveTgChat_WhenTgChatIsInDataBase_ThenSaveTgChatAndReturnPreviousEntry() {
        TgChat oldChat = new TgChat(1L, Set.of(new Link(1L, "oldChatLink")));
        mapTgChatRepository.saveTgChat(oldChat);
        TgChat newChat = new TgChat(1L, new HashSet<>());

        TgChat result = mapTgChatRepository.saveTgChat(newChat);

        assertEquals(oldChat, result);
    }

    @Test
    public void remove_WhenTgChatNotFound_ThenReturnFalse() {
        Long chatId = 1L;

        boolean result = mapTgChatRepository.remove(chatId);

        assertThat(result).isFalse();
    }

    @Test
    public void remove_WhenTgChatIsInDataBase_ThenRemoveTgChatAndReturnTrue() {
        Long chatId = 1L;
        TgChat tgChat = new TgChat(chatId, new HashSet<>());
        mapTgChatRepository.saveTgChat(tgChat);

        boolean result = mapTgChatRepository.remove(chatId);

        assertThat(result).isTrue();
    }
}

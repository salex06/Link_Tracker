package backend.academy.filters.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import backend.academy.dto.LinkUpdateInfo;
import backend.academy.filters.LinkFilter;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import backend.academy.service.ChatService;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LinkFilterByAuthorTest {
    private static LinkFilter linkFilterByAuthor;

    private static ChatService chatService;

    @BeforeAll
    public static void setup() {
        chatService = Mockito.mock(ChatService.class);

        linkFilterByAuthor = new LinkFilterByAuthor(chatService);
    }

    @Test
    public void filterChatIds_WhenNoForbiddenAuthors_ThenReturnOriginalList() {
        Set<Long> expectedChatIds = Set.of(2L, 5L);
        LinkUpdateInfo info = new LinkUpdateInfo("url", "author3", "title", "body", Instant.now(), "commonInfo");
        Long chatId5 = 5L;
        Long chatId2 = 2L;
        Long linkId = 7L;
        Link link = new Link(linkId, "url", expectedChatIds);
        TgChat chat5 = new TgChat(1L, chatId5, new HashSet<>());
        List<String> filters5 = List.of("user:author1", "user:author2");
        List<String> filters2 = List.of("user:author4");
        TgChat chat2 = new TgChat(3L, chatId2, new HashSet<>());
        when(chatService.getPlainTgChatByChatId(chatId5)).thenReturn(Optional.of(chat5));
        when(chatService.getFilters(linkId, chatId5)).thenReturn(filters5);
        when(chatService.getPlainTgChatByChatId(chatId2)).thenReturn(Optional.of(chat2));
        when(chatService.getFilters(linkId, chatId2)).thenReturn(filters2);

        List<Long> actualIds = linkFilterByAuthor.filterChatIds(info, link);

        assertEquals(expectedChatIds.stream().toList(), actualIds);
    }

    @Test
    public void filterChatIds_WhenContainsForbiddenAuthor_ThenReturnModifiedList() {
        Set<Long> chatIds = Set.of(2L, 5L);
        Set<Long> expectedChatIds = Set.of(5L);
        LinkUpdateInfo info = new LinkUpdateInfo("url", "author3", "title", "body", Instant.now(), "commonInfo");
        Long chatId5 = 5L;
        Long chatId2 = 2L;
        Long linkId = 7L;
        Link link = new Link(linkId, "url", chatIds);
        TgChat chat5 = new TgChat(1L, chatId5, new HashSet<>());
        List<String> filters5 = List.of("user:author1", "user:author2");
        List<String> filters2 = List.of("user:author3");
        TgChat chat2 = new TgChat(3L, chatId2, new HashSet<>());
        when(chatService.getPlainTgChatByChatId(chatId5)).thenReturn(Optional.of(chat5));
        when(chatService.getFilters(linkId, chatId5)).thenReturn(filters5);
        when(chatService.getPlainTgChatByChatId(chatId2)).thenReturn(Optional.of(chat2));
        when(chatService.getFilters(linkId, chatId2)).thenReturn(filters2);

        List<Long> actualIds = linkFilterByAuthor.filterChatIds(info, link);

        assertEquals(expectedChatIds.stream().toList(), actualIds);
    }
}

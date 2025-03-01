package backend.academy.service;

import backend.academy.model.Link;
import backend.academy.model.TgChat;
import backend.academy.repository.ChatRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;

public class ChatService {
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public boolean saveChat(Long chatId) {
        return chatRepository.save(chatId);
    }

    public Optional<TgChat> getChat(Long chatId) {
        return chatRepository.getById(chatId);
    }

    public Iterable<TgChat> getAllChat() {
        return chatRepository.getAll();
    }

    public boolean containsChat(Long chatId) {
        return chatRepository.getById(chatId).isPresent();
    }

    public boolean deleteChat(Long chatId) {
        return chatRepository.remove(chatId);
    }

    public boolean deleteLink(Long chatId, String url) {
        Optional<TgChat> chat = chatRepository.getById(chatId);
        if (chat.isEmpty()) {
            return false;
        }

        Set<Link> chatLinks = chat.orElseThrow().links();
        Optional<Link> link = chatLinks.stream()
                .filter(i -> Objects.equals(i.getUrl(), url))
                .limit(1)
                .findFirst();
        if (link.isEmpty()) {
            return false;
        }
        chatLinks.remove(link.orElseThrow());
        chat.orElseThrow().links(chatLinks);
        return true;
    }

    public Set<Link> getChatLinks(Long chatId) {
        Optional<TgChat> tgChat = chatRepository.getById(chatId);
        return tgChat.map(TgChat::links).orElse(null);
    }

    public void appendLinkToChat(Long chatId, Link link) {
        Optional<TgChat> chatWrapper = getChat(chatId);
        if (chatWrapper.isPresent()) {
            TgChat chat = chatWrapper.orElseThrow();
            chat.addLink(link);
        }
        saveChat(chatId);
    }
}

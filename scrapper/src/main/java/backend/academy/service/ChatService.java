package backend.academy.service;

import backend.academy.model.Link;
import backend.academy.model.TgChat;
import backend.academy.repository.ChatRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Сервис предоставляет методы для обработки запросов на регистрацию или удаление чатов и работу с ними */
@Service
public class ChatService {
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /**
     * Сохранить чат в базу данных
     *
     * @param chatId идентификатор чата
     * @return {@code true} в случае успешного сохранения, иначе - {@code false}
     */
    public boolean saveChat(Long chatId) {
        return chatRepository.save(chatId);
    }

    /**
     * Получить чат по его идентификатору
     *
     * @param chatId идентификатор чата
     * @return {@code Optional<TgChat>} если чат найден, иначе {@code Optional.empty()}
     */
    public Optional<TgChat> getChat(Long chatId) {
        return chatRepository.getById(chatId);
    }

    /**
     * Получить список всех чатов
     *
     * @return все сохраненные чаты из базы данных
     */
    public Iterable<TgChat> getAllChat() {
        return chatRepository.getAll();
    }

    /**
     * Проверить, содержится ли в базе данных чат
     *
     * @param chatId идентификатор чата
     * @return {@code true}, если содержится, иначе - {@code false}
     */
    public boolean containsChat(Long chatId) {
        return chatRepository.getById(chatId).isPresent();
    }

    /**
     * Удалить чат из базы данных
     *
     * @param chatId идентификатор чата
     * @return {@code true}, если чат удален, иначе (например, не найден) - {@code false}
     */
    public boolean deleteChat(Long chatId) {
        return chatRepository.remove(chatId);
    }

    /**
     * Удалить ссылку из списка отслеживаемых ссылок конкретного чата
     *
     * @param chatId идентификатор чата
     * @param url значение ссылки
     * @return {@code true}, если ссылка удалена, иначе (например, не найдена) - {@code false}
     */
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

    /**
     * Получить набор отслеживаемых ссылко по идентификатору чата
     *
     * @param chatId идентификатор чата
     * @return множество ссылок (если чат не найден - null)
     */
    public Set<Link> getChatLinks(Long chatId) {
        Optional<TgChat> tgChat = chatRepository.getById(chatId);
        return tgChat.map(TgChat::links).orElse(null);
    }

    /**
     * Добавить ссылку как отслеживаемую для конкретного чата
     *
     * @param chatId идентификатор чата
     * @param link значение ссылки
     */
    public void appendLinkToChat(Long chatId, Link link) {
        Optional<TgChat> chatWrapper = getChat(chatId);
        if (chatWrapper.isPresent()) {
            TgChat chat = chatWrapper.orElseThrow();
            chat.addLink(link);
        }
        saveChat(chatId);
    }
}

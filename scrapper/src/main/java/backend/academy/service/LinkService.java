package backend.academy.service;

import backend.academy.clients.Client;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LinkService {
    Page<Link> getAllLinks(Pageable pageable, Duration duration);

    Optional<Link> getLink(Long chatId, String linkValue);

    Link saveLink(Link link, TgChat chat);

    Set<Link> getAllLinksByChatId(Long chatId);

    Set<Long> getChatIdsListeningToLink(String url);

    /**
     * Проверить, является ли переданная ссылка корректной
     *
     * @param link строковое значение ссылки
     * @return {@code true}, если ссылка поддерживается, {@code false} - иначе
     */
    default boolean validateLink(List<Client> availableClients, String link) {
        for (Client client : availableClients) {
            if (client.supportLink(link)) {
                return true;
            }
        }

        return false;
    }

    void updateLastUpdateTime(Link link, Instant updateTime);

    List<Link> getAllLinksByChatIdAndTag(Long id, String tag);
}

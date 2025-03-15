package backend.academy.service;

import backend.academy.clients.Client;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LinkService {
    Iterable<Link> getAllLinks();

    Optional<Link> getLink(Long chatId, String linkValue);

    Link saveLink(Link link, TgChat chat);

    Link updateChats(Link link, Set<TgChat> newChats);

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
}

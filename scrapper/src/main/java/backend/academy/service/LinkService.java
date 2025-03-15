package backend.academy.service;

import backend.academy.clients.Client;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LinkService {
    Iterable<Link> getAllLinks();

    Optional<Link> getLinkById(Long id);

    Optional<Link> getLinkByValue(Long chatId, String url);

    Link saveLink(Link link);

    Link updateChats(Link link, Set<TgChat> newChats);

    Set<Link> getAllLinksByChatId(Long chatId);

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

    Set<Long> getChatIdsListeningToLink(String url);
}

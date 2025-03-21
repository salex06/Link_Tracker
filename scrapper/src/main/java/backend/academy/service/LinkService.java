package backend.academy.service;

import backend.academy.clients.Client;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Интерфейс сервиса для реализации бизнес-логики работы с ссылками на ресурсы */
public interface LinkService {
    /**
     * Получить ссылки с использованием механизма пагинации
     *
     * @param pageable параметры страницы
     * @return страница с ссылками
     */
    Page<Link> getAllLinks(Pageable pageable);

    /**
     * Получить ссылку по идентификатору чата и значению ссылки
     *
     * @param chatId идентификатор чата
     * @param linkValue значение ссылки
     * @return {@code Optional<Link>}, если ссылка найдена, иначе - {@code Optional.empty()}
     */
    Optional<Link> getLink(Long chatId, String linkValue);

    /**
     * Сохранить ссылку для данного чата
     *
     * @param link ссылка на ресурс
     * @param chat телеграм-чат
     * @return объект класса {@code Link} - сохраненная ссылка
     */
    Link saveLink(Link link, TgChat chat);

    /**
     * Получить все ссылки по идентификатору чата
     *
     * @param chatId идентификатор чата
     * @return набор ссылок, которые отслеживает чат
     */
    Set<Link> getAllLinksByChatId(Long chatId);

    /**
     * Получить идентификаторы чатов, которые отслеживают данную ссылку
     *
     * @param url значение ссылки
     * @return набор идентификатор чатов
     */
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

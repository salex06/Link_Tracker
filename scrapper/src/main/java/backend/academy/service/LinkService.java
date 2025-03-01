package backend.academy.service;

import backend.academy.clients.Client;
import backend.academy.clients.ClientManager;
import backend.academy.model.Link;
import backend.academy.repository.LinkRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Сервис для обработки ссылок, которые передаются между пользователем и хранилищем */
@Service
public class LinkService {
    private final LinkRepository linkRepository;
    private final ClientManager clientManager;

    @Autowired
    public LinkService(LinkRepository linkRepository, ClientManager clientManager) {
        this.linkRepository = linkRepository;
        this.clientManager = clientManager;
    }

    /**
     * Возвращает все ссылки из хранилища в виде списка
     *
     * @return объекты класса Link из хранилища
     */
    public List<Link> getAllLinks() {
        return linkRepository.getAllLinks();
    }

    /**
     * Получить ссылку по Id
     *
     * @param id идентификатор ссылки
     * @return {@code Optional<Link>} - если ссылка найдена, иначе - {@code Optional.empty()}
     */
    public Optional<Link> getLink(Long id) {
        return linkRepository.getById(id);
    }

    /**
     * Выполняет поиск ссылки в хранилище по имеющимся данным о ссылке (url, tags, filters)
     *
     * @param link данные о ссылке в виде объекта Link (при поиске не учитывается ID переданной ссылки)
     * @return объект класса Link, если ссылка найдена, иначе - null
     */
    public Link findLink(Link link) {
        List<Link> links = linkRepository.getAllLinks();
        for (Link dbLink : links) {
            if (Objects.equals(dbLink, link)) {
                return dbLink;
            }
        }
        return null;
    }

    /**
     * Добавить ссылку в хранилище
     *
     * @param link ссылка, которую требуется сохранить
     * @return объект класса Link, который уже был в хранилище. Если ссылка добавлена впервые - null
     */
    public Link saveLink(Link link) {
        return linkRepository.save(link);
    }

    /**
     * Выполняет поиск в хранилище переданной ссылки и сохраняет, если ссылка там отстутствуем
     *
     * @param link ссылка для сохранения
     * @return объект класса Link. Если ссылка уже была в хранилище, то возвращается эта ссылка, иначе - добавляется
     *     ссылка, переданная в качестве параметра
     */
    public Link saveOrGetLink(Link link) {
        Link inDataBaseLink = findLink(link);
        if (inDataBaseLink != null) {
            return inDataBaseLink;
        }
        saveLink(link);
        return link;
    }

    /**
     * Добавляет в объект класса Link информацию о новом чате, который отслеживает ссылку
     *
     * @param chatId чат, в котором будет отслеживаться ссылка
     * @param source ссылка
     * @return {@code true} - если информация добавлена, {@code false} - если ссылка не найдена
     */
    public boolean appendChatToLink(Long chatId, Link source) {
        Optional<Link> linkFromDatabaseWrapper = linkRepository.getById(source.getId());
        if (linkFromDatabaseWrapper.isPresent()) {
            Link link = linkFromDatabaseWrapper.orElseThrow();
            Set<Long> chats = link.getTgChatIds();
            chats.add(chatId);
            link.setTgChatIds(chats);
            linkRepository.save(link);
            return true;
        }
        return false;
    }

    /**
     * Удаляет чат из списка отслеживающих данную ссылку
     *
     * @param chatId идентификатор чата
     * @param link ссылка, запись об отслеживании в которой нобходимо удалить
     * @return {@code true}, если ссылка найдена и из неё успешно удалена информация о чате, {@code false} - иначе
     */
    public boolean deleteChatFromLink(Long chatId, Link link) {
        Link databaseLink = findLink(link);
        if (databaseLink == null) {
            return false;
        }

        Set<Long> tgChatIds = link.getTgChatIds();
        if (tgChatIds.remove(chatId)) {
            link.setTgChatIds(tgChatIds);
            linkRepository.save(link);
            return true;
        }
        return false;
    }

    /**
     * Проверить, является ли переданная ссылка корректной
     *
     * @param link строковое значение ссылки
     * @return {@code true}, если ссылка поддерживается, {@code false} - иначе
     */
    public boolean validateLink(String link) {
        for (Client client : clientManager.availableClients()) {
            if (client.supportLink(link)) {
                return true;
            }
        }
        return false;
    }
}

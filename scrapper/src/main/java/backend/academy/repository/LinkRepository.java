package backend.academy.repository;

import backend.academy.model.Link;
import java.util.List;
import java.util.Optional;

/** Интерфейс предоставляет набор методов для доступа к данным из БД о ссылках на ресурсы */
public interface LinkRepository {
    /**
     * Получить ссылку по её идентификатору
     *
     * @param id идентификатор ссылки
     * @return {@code Optional<Link>} если ссылка найдена, иначе - {@code Optional.empty()}
     */
    Optional<Link> getById(Long id);

    /**
     * Получить все ссылки из БД
     *
     * @return список всех ссылок из БД
     */
    List<Link> getAllLinks();

    /**
     * Сохранить ссылку в БД
     *
     * @param link ссылка, которую требуется сохранить
     * @return {@code Link} - объект класса Link - сохраненная ссылка
     */
    Link save(Link link);
}

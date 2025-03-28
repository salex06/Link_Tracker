package backend.academy.repository.orm;

import backend.academy.model.orm.OrmLink;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией о ссылках, сохраненных в БД */
@Repository
public interface OrmLinkRepository extends JpaRepository<OrmLink, Long> {
    /**
     * Получить все ссылки из базы данных с использованием пагинации
     *
     * @param pageable параметры текущей страницы записей
     * @return страница с набором ссылок
     */
    @NotNull
    Page<OrmLink> findAll(@NotNull Pageable pageable);

    /**
     * Найти ссылку по её значению
     *
     * @param linkValue значение ссылки
     * @return {@code Optional<OrmLink>}, если ссылка найдена, иначе - {@code Optional.empty()}
     */
    Optional<OrmLink> findByLinkValue(String linkValue);

    /**
     * Обновить запись ссылки
     *
     * @param id идентификатор ссылки
     * @param linkValue значение ссылки (обновляется)
     * @param lastUpdate время последнего обновления (обновляется)
     */
    @Modifying
    @Transactional
    @Query("UPDATE OrmLink link SET link.linkValue = :linkValue, link.lastUpdate = :lastUpdate WHERE link.id = :id")
    void updateLink(
            @Param("id") Long id, @Param("linkValue") String linkValue, @Param("lastUpdate") Instant lastUpdate);
}

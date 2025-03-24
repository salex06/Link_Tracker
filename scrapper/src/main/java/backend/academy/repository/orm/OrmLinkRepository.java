package backend.academy.repository.orm;

import backend.academy.model.orm.OrmLink;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

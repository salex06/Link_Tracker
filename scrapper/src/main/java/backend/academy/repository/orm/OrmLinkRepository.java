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
    @NotNull
    Page<OrmLink> findAll(@NotNull Pageable pageable);

    Optional<OrmLink> findByLinkValue(String linkValue);
}

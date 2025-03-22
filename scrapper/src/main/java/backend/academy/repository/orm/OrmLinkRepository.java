package backend.academy.repository.orm;

import backend.academy.model.orm.OrmLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией о ссылках, сохраненных в БД */
@Repository
public interface OrmLinkRepository extends JpaRepository<OrmLink, Long> {}

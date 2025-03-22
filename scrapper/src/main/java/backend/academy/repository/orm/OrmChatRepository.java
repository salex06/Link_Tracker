package backend.academy.repository.orm;

import backend.academy.model.orm.OrmChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Репозиторий для взаимодействия с информацией о чатах, сохраненных в БД */
@Repository
public interface OrmChatRepository extends JpaRepository<OrmChat, Long> {}

package backend.academy.repository;

import backend.academy.model.TgChat;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    Optional<TgChat> getById(Long id);

    List<TgChat> getAll();

    boolean save(Long id);

    boolean remove(Long id);
}

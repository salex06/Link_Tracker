package backend.academy.repository;

import backend.academy.model.TgChat;
import java.util.Optional;

public interface ChatRepository {
    Optional<TgChat> getById(Long id);

    Iterable<TgChat> getAll();

    boolean save(Long id);

    boolean remove(Long id);
}

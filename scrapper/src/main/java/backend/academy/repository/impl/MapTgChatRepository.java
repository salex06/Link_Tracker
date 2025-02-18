package backend.academy.repository.impl;

import backend.academy.model.TgChat;
import backend.academy.repository.ChatRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MapTgChatRepository implements ChatRepository {
    private final Map<Long, TgChat> database = new HashMap<>();

    @Override
    public Optional<TgChat> getById(Long id) {
        if (database.containsKey(id)) {
            return Optional.of(database.get(id));
        }
        return Optional.empty();
    }

    @Override
    public Iterable<TgChat> getAll() {
        return database.values();
    }

    @Override
    public boolean save(Long id) {
        if (database.containsKey(id)) {
            return false;
        }
        database.put(id, new TgChat(id, new ArrayList<>()));
        return true;
    }

    @Override
    public boolean remove(Long id) {
        if (!database.containsKey(id)) {
            return false;
        }
        database.remove(id);
        return true;
    }
}

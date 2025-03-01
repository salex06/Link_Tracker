package backend.academy.repository.impl;

import backend.academy.model.TgChat;
import backend.academy.repository.ChatRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Класс представляет собой хранилище чатов на основе контейнера "Словарь" */
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
    public List<TgChat> getAll() {
        return database.values().stream().toList();
    }

    @Override
    public boolean save(Long id) {
        if (database.containsKey(id)) {
            return false;
        }
        database.put(id, new TgChat(id, new HashSet<>()));
        return true;
    }

    @Override
    public TgChat saveTgChat(TgChat tgChat) {
        TgChat previousTgChat = null;
        if (database.containsKey(tgChat.id())) {
            previousTgChat = database.get(tgChat.id());
        }
        database.put(tgChat.id(), tgChat);
        return previousTgChat;
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

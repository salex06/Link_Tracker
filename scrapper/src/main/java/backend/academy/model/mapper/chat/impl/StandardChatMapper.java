package backend.academy.model.mapper.chat.impl;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.mapper.chat.ChatMapper;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StandardChatMapper implements ChatMapper {
    @Override
    public TgChat toPlainTgChat(JdbcTgChat chat, Set<Link> links) {
        return new TgChat(chat.getId(), chat.getChatId(), links);
    }

    @Override
    public JdbcTgChat toJdbcTgChat(TgChat chat) {
        return new JdbcTgChat(chat.getInternalId(), chat.getChatId());
    }

    @Override
    public TgChat toPlainTgChat(OrmChat chat, Set<Link> links) {
        return new TgChat(chat.getId(), chat.getChatId(), links);
    }

    @Override
    public OrmChat toOrmChat(TgChat chat) {
        return new OrmChat(chat.getInternalId(), chat.getChatId());
    }
}

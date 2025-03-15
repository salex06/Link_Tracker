package backend.academy.model.mapper.chat;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public interface ChatMapper {
    TgChat toPlainTgChat(JdbcTgChat chat, Set<Link> links);

    JdbcTgChat toJdbcTgChat(TgChat chat);
}

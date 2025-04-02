package backend.academy.model.mapper.chat;

import backend.academy.model.jdbc.JdbcTgChat;
import backend.academy.model.orm.OrmChat;
import backend.academy.model.plain.Link;
import backend.academy.model.plain.TgChat;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public interface ChatMapper {
    /**
     * Преобразовать Jdbc чат в обычный чат
     *
     * @param chat jdbc-чат, специфический для Spring JDBC
     * @param links набор ссылок, которые отслеживает чат
     * @return обычный чат
     */
    TgChat toPlainTgChat(JdbcTgChat chat, Set<Link> links);

    /**
     * Преобразовать обычный чат в Jdbc-чат
     *
     * @param chat обычный чат
     * @return сконвертированная Jdbc-версия чата
     */
    JdbcTgChat toJdbcTgChat(TgChat chat);

    /**
     * Преобразовать orm-чат в обычный чат
     *
     * @param chat чат, специфический для Spring JPA (Hibernate)
     * @param links набор ссылок, которые отслеживает чат
     * @return обычный чат
     */
    TgChat toPlainTgChat(OrmChat chat, Set<Link> links);

    /**
     * Преобразовать обычный чат в Orm-чат
     *
     * @param chat обычный чат
     * @return сконвертированная Orm-версия чата
     */
    OrmChat toOrmChat(TgChat chat);
}

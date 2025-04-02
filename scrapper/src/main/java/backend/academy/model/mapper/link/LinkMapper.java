package backend.academy.model.mapper.link;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public interface LinkMapper {
    /**
     * Преобразовать Jdbc ссылку в обычную
     *
     * @param jdbcLink ссылка, специфическая для реализации Spring JDBC
     * @param tags теги ссылки
     * @param filters фильтры ссылки
     * @param tgChatIds идентификаторы чатов, отслеживающих ссылку
     * @return обычная ссылка
     */
    Link toPlainLink(JdbcLink jdbcLink, List<String> tags, List<String> filters, Set<Long> tgChatIds);

    /**
     * Преобразовать обычную ссылку в Jdbc ссылку
     *
     * @param link объект "обобщенной" ссылки
     * @return ссылка, специфическая для реализации Spring JDBC
     */
    JdbcLink toJdbcLink(Link link);

    /**
     * Преобразовать ORM (JPA/Hibernate) ссылку в обычную
     *
     * @param ormLink orm-сссылка
     * @param tags теги
     * @param filters фильтры
     * @param tgChatIds идентификаторы чатов, отслеживающих ссылку
     * @return обычная ссылка
     */
    Link toPlainLink(OrmLink ormLink, List<String> tags, List<String> filters, Set<Long> tgChatIds);

    /**
     * Преобразовать обычную ссылку в Orm-ссылку
     *
     * @param link обычная ссылка
     * @return ссылка, специфическая для реализации Spring JPA (Hibernate)
     */
    OrmLink toOrmLink(Link link);
}

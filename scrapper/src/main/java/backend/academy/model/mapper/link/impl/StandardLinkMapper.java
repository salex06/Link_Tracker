package backend.academy.model.mapper.link.impl;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.mapper.link.LinkMapper;
import backend.academy.model.orm.OrmLink;
import backend.academy.model.plain.Link;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class StandardLinkMapper implements LinkMapper {
    @Override
    public Link toPlainLink(JdbcLink jdbcLink, List<String> tags, List<String> filters, Set<Long> tgChatIds) {
        return new Link(
                jdbcLink.getId(),
                jdbcLink.getUrl(),
                tags,
                filters,
                tgChatIds,
                jdbcLink.getType(),
                jdbcLink.getLastUpdateTime());
    }

    @Override
    public JdbcLink toJdbcLink(Link link) {
        return new JdbcLink(link.getId(), link.getUrl(), link.getLastUpdateTime(), link.getType());
    }

    @Override
    public Link toPlainLink(OrmLink ormLink, List<String> tags, List<String> filters, Set<Long> tgChatIds) {
        return new Link(
                ormLink.getId(),
                ormLink.getLinkValue(),
                tags,
                filters,
                tgChatIds,
                ormLink.getType(),
                ormLink.getLastUpdate());
    }

    @Override
    public OrmLink toOrmLink(Link link) {
        return new OrmLink(link.getId(), link.getUrl(), link.getLastUpdateTime(), link.getType());
    }
}

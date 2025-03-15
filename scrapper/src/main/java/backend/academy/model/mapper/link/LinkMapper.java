package backend.academy.model.mapper.link;

import backend.academy.model.jdbc.JdbcLink;
import backend.academy.model.plain.Link;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public interface LinkMapper {
    Link toPlainLink(JdbcLink jdbcLink, List<String> tags, List<String> filters, Set<Long> tgChatIds);

    JdbcLink toJdbcLink(Link link);
}

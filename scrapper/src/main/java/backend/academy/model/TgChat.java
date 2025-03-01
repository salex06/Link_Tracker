package backend.academy.model;

import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Модель чата в телеграме, хранящая уникальный идентификатор и набор ссылок, которые отслеживаются данным чатом */
@AllArgsConstructor
@Getter
public class TgChat {
    private final Long id;

    @Setter
    private Set<Link> links;

    public void addLink(Link link) {
        links.add(link);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TgChat tgChat)) return false;
        return Objects.equals(id, tgChat.id) && Objects.equals(links, tgChat.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, links);
    }
}

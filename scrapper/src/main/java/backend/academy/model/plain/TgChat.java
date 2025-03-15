package backend.academy.model.plain;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class TgChat {
    private final Long internalId;

    private final Long chatId;

    @Setter
    private Set<Link> links;

    public void addLink(Link link) {
        links.add(link);
    }
}

package backend.academy.model.plain;

import java.time.LocalTime;
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

    private final LocalTime sendAt;

    @Setter
    private Set<Link> links;

    public void addLink(Link link) {
        links.add(link);
    }

    public TgChat(Long internalId, Long chatId, Set<Link> links) {
        this.internalId = internalId;
        this.chatId = chatId;
        this.links = links;
        this.sendAt = null;
    }
}

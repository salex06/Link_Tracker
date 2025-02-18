package backend.academy.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class TelegramClient implements Serializable {
    private final Long chatId;
    private final Long id;
    private final String firstName;

    @Setter
    private Set<Link> trackedLinks;

    public TelegramClient(Long chatId, Long id, String firstName) {
        this.id = id;
        this.firstName = firstName;
        this.chatId = chatId;
        trackedLinks = new HashSet<>();
    }
}

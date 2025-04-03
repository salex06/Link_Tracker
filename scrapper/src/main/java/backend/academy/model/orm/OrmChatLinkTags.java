package backend.academy.model.orm;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_link_tags")
@NoArgsConstructor
@Getter
@Setter
public class OrmChatLinkTags {
    @EmbeddedId
    private OrmChatLinkTagsIdEmbedded id;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private OrmChat chat;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private OrmLink link;

    public OrmChatLinkTags(OrmChat chat, OrmLink link, String tagValue) {
        this.chat = chat;
        this.link = link;
        this.id = new OrmChatLinkTagsIdEmbedded(chat.getId(), link.getId(), tagValue);
    }
}

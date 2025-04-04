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
@Table(name = "tg_chat_link")
@NoArgsConstructor
@Getter
public class OrmChatLink {
    @EmbeddedId
    @Setter
    private OrmChatLinkIdEmbedded id;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "tg_chat_id", referencedColumnName = "id")
    private OrmChat chat;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id", referencedColumnName = "id")
    private OrmLink link;

    public OrmChatLink(OrmChat chat, OrmLink link) {
        this.chat = chat;
        this.link = link;
        this.id = new OrmChatLinkIdEmbedded(chat.getId(), link.getId());
    }

    public void setChat(OrmChat chat) {
        this.chat = chat;
        this.id = new OrmChatLinkIdEmbedded(chat.getId(), this.link.getId());
    }

    public void setLink(OrmLink link) {
        this.link = link;
        this.id = new OrmChatLinkIdEmbedded(this.chat.getId(), link.getId());
    }
}

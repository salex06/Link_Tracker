package backend.academy.model.orm;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 * Модель связующей таблицы между ссылкой на ресурс и чатом. Данная сущность содержит информацию о чате и ссылке,
 * которую данный чат отслеживает
 */
@Entity
@Table(name = "tg_chat_link")
@NoArgsConstructor
public class OrmChatLink {
    @EmbeddedId
    private OrmChatLinkIdEmbedded id;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "tg_chat_id")
    private OrmChat chat;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private OrmLink link;

    public OrmChatLink(OrmChat chat, OrmLink link) {
        this.chat = chat;
        this.link = link;
        this.id = new OrmChatLinkIdEmbedded(chat.getChatId(), link.getId());
    }

    public OrmChatLinkIdEmbedded getId() {
        return id;
    }

    public void setId(OrmChatLinkIdEmbedded id) {
        this.id = id;
    }

    public OrmChat getChat() {
        return chat;
    }

    public void setChat(OrmChat chat) {
        this.chat = chat;
        this.id = new OrmChatLinkIdEmbedded(chat.getChatId(), this.link.getId());
    }

    public OrmLink getLink() {
        return link;
    }

    public void setLink(OrmLink link) {
        this.link = link;
        this.id = new OrmChatLinkIdEmbedded(this.chat.getChatId(), link.getId());
    }
}

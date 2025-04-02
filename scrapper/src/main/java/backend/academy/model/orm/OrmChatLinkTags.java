package backend.academy.model.orm;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_link_tags")
@NoArgsConstructor
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

    public OrmChatLinkTagsIdEmbedded getId() {
        return id;
    }

    public void setId(OrmChatLinkTagsIdEmbedded id) {
        this.id = id;
    }

    public OrmChat getChat() {
        return chat;
    }

    public void setChat(OrmChat chat) {
        this.chat = chat;
    }

    public OrmLink getLink() {
        return link;
    }

    public void setLink(OrmLink link) {
        this.link = link;
    }

    public String getTagValue() {
        return id.getTagValue();
    }

    public void setTagValue(String tagValue) {
        this.id.setTagValue(tagValue);
    }
}

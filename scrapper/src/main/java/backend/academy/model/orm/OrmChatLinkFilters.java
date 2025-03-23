package backend.academy.model.orm;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/** Сущность для хранения данных о фильтрах, связанных с конкретными чатоми и ссылками */
@Entity
@Table(name = "chat_link_filters")
@NoArgsConstructor
public class OrmChatLinkFilters {
    @EmbeddedId
    private OrmChatLinkFiltersIdEmbedded id;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private OrmChat chat;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private OrmLink link;

    public OrmChatLinkFilters(OrmChat chat, OrmLink link, String filterValue) {
        this.chat = chat;
        this.link = link;
        this.id = new OrmChatLinkFiltersIdEmbedded(chat.getId(), link.getId(), filterValue);
    }

    public OrmChatLinkFiltersIdEmbedded getId() {
        return id;
    }

    public void setId(OrmChatLinkFiltersIdEmbedded id) {
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

    public String getFilterValue() {
        return id.getFilterValue();
    }

    public void setFilterValue(String filterValue) {
        id.setFilterValue(filterValue);
    }
}

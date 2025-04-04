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
@Table(name = "chat_link_filters")
@NoArgsConstructor
@Getter
@Setter
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
}

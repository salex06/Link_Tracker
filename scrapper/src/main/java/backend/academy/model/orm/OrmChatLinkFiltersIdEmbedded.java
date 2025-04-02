package backend.academy.model.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class OrmChatLinkFiltersIdEmbedded implements Serializable {
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "filter_value")
    private String filterValue;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrmChatLinkFiltersIdEmbedded that)) return false;
        return Objects.equals(chatId, that.chatId)
                && Objects.equals(linkId, that.linkId)
                && Objects.equals(filterValue, that.filterValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, linkId, filterValue);
    }
}

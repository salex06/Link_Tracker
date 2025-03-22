package backend.academy.model.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Класс представляет модель первичного составного ключа сущности OrmChatLinkTags. Хранит информацию об идентификаторах
 * чата и ссылки, о значении тега
 */
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class OrmChatLinkTagsIdEmbedded implements Serializable {
    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "tag_value")
    private String tagValue;

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

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrmChatLinkTagsIdEmbedded that)) return false;
        return Objects.equals(chatId, that.chatId)
                && Objects.equals(linkId, that.linkId)
                && Objects.equals(tagValue, that.tagValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, linkId, tagValue);
    }
}

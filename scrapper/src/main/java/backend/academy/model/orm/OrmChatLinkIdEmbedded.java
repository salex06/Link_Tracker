package backend.academy.model.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Класс для представления составного первичного ключа сущности OrmChatLink. Хранит информацию об идентификаторе чата и
 * идентификаторе связанной ссылки
 */
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class OrmChatLinkIdEmbedded implements Serializable {
    @Column(name = "tg_chat_id")
    private Long chatId;

    @Column(name = "link_id")
    private Long linkId;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrmChatLinkIdEmbedded that = (OrmChatLinkIdEmbedded) o;
        return Objects.equals(chatId, that.chatId) && Objects.equals(linkId, that.linkId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, linkId);
    }
}

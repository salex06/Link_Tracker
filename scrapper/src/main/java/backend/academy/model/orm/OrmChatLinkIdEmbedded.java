package backend.academy.model.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrmChatLinkIdEmbedded implements Serializable {
    @Column(name = "tg_chat_id")
    private Long chatId;

    @Column(name = "link_id")
    private Long linkId;
}

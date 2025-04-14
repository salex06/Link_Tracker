package backend.academy.model.jdbc;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table("tg_chat")
public class JdbcTgChat {
    @Id
    private Long id;

    @Column("chat_id")
    private Long chatId;

    @Column("send_at")
    private LocalTime sendAt;

    public JdbcTgChat(Long id, Long chatId) {
        this.id = id;
        this.chatId = chatId;
        this.sendAt = null;
    }
}

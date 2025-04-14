package backend.academy.model.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tg_chat")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrmChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", unique = true, nullable = false)
    private Long chatId;

    @Column(name = "send_at")
    private LocalTime sendAt;

    public OrmChat(Long chatId) {
        this.chatId = chatId;
    }

    public OrmChat(Long id, Long chatId) {
        this.id = id;
        this.chatId = chatId;
        this.sendAt = null;
    }
}

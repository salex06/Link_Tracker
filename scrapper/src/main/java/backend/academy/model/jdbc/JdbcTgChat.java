package backend.academy.model.jdbc;

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
}

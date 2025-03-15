package backend.academy.model.jdbc;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Table("tg_chat")
public class JdbcTgChat {
    @Id
    private final Long id;

    @Column("chat_id")
    private final Long chatId;
}

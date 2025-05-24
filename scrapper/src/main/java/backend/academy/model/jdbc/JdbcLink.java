package backend.academy.model.jdbc;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode
@Table("link")
@Getter
@Setter
public class JdbcLink {
    @Id
    private Long id;

    @Column("link_value")
    private String url;

    @Column("last_update")
    private Instant lastUpdateTime = Instant.now();

    @Column("type")
    private String type;

    public JdbcLink() {}

    public JdbcLink(Long id, String url) {
        this.id = id;
        this.url = url;
    }

    public JdbcLink(Long id, String url, Instant lastUpdateTime) {
        this.id = id;
        this.url = url;
        this.lastUpdateTime = lastUpdateTime;
    }

    public JdbcLink(Long id, String url, Instant lastUpdateTime, String type) {
        this.id = id;
        this.url = url;
        this.lastUpdateTime = lastUpdateTime;
        this.type = type;
    }
}

package backend.academy.model.jdbc;

import java.time.Instant;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Модель ссылки, специфическая для технологии Spring JDBC */
@EqualsAndHashCode
@Table("link")
public class JdbcLink {
    @Id
    private Long id;

    @Column("link_value")
    private String url;

    @Column("last_update")
    private Instant lastUpdateTime = Instant.now();

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Instant lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

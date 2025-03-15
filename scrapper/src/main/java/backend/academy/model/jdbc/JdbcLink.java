package backend.academy.model.jdbc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@EqualsAndHashCode
@Table("link")
public class JdbcLink {
    @Id
    private Long id;

    @Column("link_value")
    private String url;

    @Column("last_update")
    private LocalDateTime lastUpdateTime = LocalDateTime.now(ZoneId.of("UTC"));

    public JdbcLink() {}

    public JdbcLink(Long id, String url) {
        this.id = id;
        this.url = url;
    }

    public JdbcLink(Long id, String url, LocalDateTime lastUpdateTime) {
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

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

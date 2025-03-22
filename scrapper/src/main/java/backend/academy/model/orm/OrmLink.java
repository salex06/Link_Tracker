package backend.academy.model.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Модель orm-сущности, представляющей ссылку. Хранит идентификатор ссылки, значение ссылки и дату последней проверки
 * обновлений
 */
@Entity
@Table(name = "link")
@NoArgsConstructor
@AllArgsConstructor
public class OrmLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "link_value", nullable = false)
    private String linkValue;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate = LocalDateTime.now();

    public OrmLink(String linkValue) {
        this.linkValue = linkValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLinkValue() {
        return linkValue;
    }

    public void setLinkValue(String linkValue) {
        this.linkValue = linkValue;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}

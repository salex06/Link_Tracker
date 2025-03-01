package backend.academy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Модель ссылки, содержащая информацию о тегах, фильтрах, связанных чатах, а также значение самой ссылки и её
 * идентификатор
 */
public class Link {
    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;
    private Set<Long> tgChatIds;

    @JsonIgnore
    private LocalDateTime lastUpdateTime;

    private static final AtomicLong nextId = new AtomicLong();

    /**
     * Конструктор со всеми параметрами
     *
     * @param id идентификатор ссылки
     * @param url значение ссылки
     * @param tags теги
     * @param filters фильтры
     * @param tgChatIds связанные чаты в телеграме
     */
    public Link(Long id, String url, List<String> tags, List<String> filters, Set<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.tgChatIds = tgChatIds;
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Конструктор с основными параметрами (без тегов и фильтров)
     *
     * @param id идентификатор ссылки
     * @param url значение ссылки
     * @param tgChatIds привязанные чаты в телеграме
     */
    public Link(Long id, String url, Set<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tgChatIds = tgChatIds;
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Конструктор с двумя параметрами
     *
     * @param id идентификатор ссылки
     * @param url значение ссылки
     */
    public Link(Long id, String url) {
        this.id = id;
        this.url = url;
        this.tgChatIds = new HashSet<>();
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Конструктор, устанавливающий значение ссылки (id рассчитывается автоматически)
     *
     * @param url значение ссылки
     */
    public Link(String url) {
        this.id = nextId.incrementAndGet();
        this.url = url;
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.tgChatIds = new HashSet<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    /** Конструктор по умолчанию */
    public Link() {
        this.id = null;
        this.url = null;
        this.tgChatIds = new HashSet<>();
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Set<Long> getTgChatIds() {
        return tgChatIds;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTgChatIds(Set<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link link)) return false;
        return Objects.equals(url, link.url)
                && Objects.equals(tags, link.tags)
                && Objects.equals(filters, link.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, tags, filters);
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}

package backend.academy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;

@Getter
public class Link {
    private Long id;
    private String url;
    private List<Long> tgChatIds;
    private List<String> tags;
    private List<String> filters;
    private static final AtomicLong nextId = new AtomicLong();

    public Link(Long id, String url, List<Long> tgChatIds, List<String> tags, List<String> filters) {
        this.id = id;
        this.url = url;
        this.tgChatIds = tgChatIds;
        this.tags = tags;
        this.filters = filters;
    }

    public Link(Long id, String url, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tgChatIds = tgChatIds;
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
    }

    public Link() {
        this.id = null;
        this.url = null;
        this.tgChatIds = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.tags = new ArrayList<>();
    }

    /**
     * Конструктор с одним параметром с автоматическим присваиванием идентификатора
     *
     * @param url значение ссылки
     */
    public Link(String url) {
        this.id = nextId.incrementAndGet();
        this.url = url;
        this.tgChatIds = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link link)) return false;
        return Objects.equals(id, link.id)
                && Objects.equals(url, link.url)
                && Objects.equals(tgChatIds, link.tgChatIds)
                && Objects.equals(tags, link.tags)
                && Objects.equals(filters, link.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, tgChatIds, tags, filters);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}

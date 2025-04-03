package backend.academy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
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
}

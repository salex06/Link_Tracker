package backend.academy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class Link {
    private Long id;
    private String url;

    @JsonIgnore
    private LocalDateTime lastUpdateTime = null;

    private List<Long> tgChatIds;
    private static final AtomicLong nextId = new AtomicLong();

    public Link(Long id, String url, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tgChatIds = tgChatIds;
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Link(Long id, String url) {
        this.id = id;
        this.url = url;
        this.tgChatIds = new ArrayList<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Link() {
        this.id = null;
        this.url = null;
        this.tgChatIds = new ArrayList<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Link(String url) {
        this.id = nextId.incrementAndGet();
        this.url = url;
        this.tgChatIds = new ArrayList<>();
        this.lastUpdateTime = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public List<Long> getTgChatIds() {
        return tgChatIds;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
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

    public void setLastUpdateTime(LocalDateTime lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link link)) return false;
        return Objects.equals(url, link.url);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(url);
    }
}

package backend.academy.model.plain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(exclude = {"id", "tgChatIds", "lastUpdateTime"})
public class Link {
    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;
    private Set<Long> tgChatIds;

    @JsonIgnore
    private Instant lastUpdateTime;

    public Link(
            Long id, String url, List<String> tags, List<String> filters, Set<Long> tgChatIds, Instant lastUpdateTime) {
        this.id = id;
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.tgChatIds = tgChatIds;
        this.lastUpdateTime = lastUpdateTime;
    }

    public Link(Long id, String url, List<String> tags, List<String> filters, Set<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.tgChatIds = tgChatIds;
        this.lastUpdateTime = Instant.now();
    }

    public Link(Long id, String url, Set<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.tgChatIds = tgChatIds;
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.lastUpdateTime = Instant.now();
    }

    public Link(Long id, String url) {
        this.id = id;
        this.url = url;
        this.tgChatIds = new HashSet<>();
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.lastUpdateTime = Instant.now();
    }

    public Link() {
        this.id = null;
        this.url = null;
        this.tgChatIds = new HashSet<>();
        this.tags = new ArrayList<>();
        this.filters = new ArrayList<>();
        this.lastUpdateTime = Instant.now();
    }
}

package backend.academy.dto;

import java.util.List;

public record LinkUpdate(Long id, String url, String description, String updateTime, List<Long> tgChatIds) {
    public LinkUpdate() {
        this(null, null, null, null, null);
    }
}

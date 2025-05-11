package backend.academy.notifications.fallback;

import backend.academy.dto.LinkUpdate;

public interface FallbackSender {
    void send(LinkUpdate update);
}

package backend.academy.notifications;

import backend.academy.dto.LinkUpdate;

public interface NotificationSender {
    void send(LinkUpdate update);
}

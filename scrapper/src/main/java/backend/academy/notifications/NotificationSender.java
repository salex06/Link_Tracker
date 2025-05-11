package backend.academy.notifications;

import backend.academy.dto.LinkUpdate;

public interface NotificationSender {
    /**
     * Отправить уведомление об обновлении
     *
     * @param update DTO, хранящий информацию об обновлении
     */
    String send(LinkUpdate update);
}

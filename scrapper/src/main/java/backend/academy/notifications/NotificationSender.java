package backend.academy.notifications;

import backend.academy.dto.LinkUpdate;

/** Интерфейс предоставляет контракт для отправки уведомлений с помощью различных способов (HTTP, Kafka) */
public interface NotificationSender {
    /**
     * Отправить уведомление об обновлении
     *
     * @param update DTO, хранящий информацию об обновлении
     */
    void send(LinkUpdate update);
}

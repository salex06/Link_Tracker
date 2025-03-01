package backend.academy.dto;

import java.util.List;

/**
 * Объект передачи данных для отправки запроса на уведомление пользователей об обновлении ссылки
 *
 * @param id идентификатор ссылки
 * @param url значение ссылки
 * @param description уведомление
 * @param tgChatIds идентификаторы чатов, отслеживающих ссылку
 */
public record LinkUpdate(Long id, String url, String description, List<Long> tgChatIds) {}

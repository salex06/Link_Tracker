package backend.academy.dto;

import java.util.List;

/**
 * Объект передачи данных для запроса на добавление ссылки
 *
 * @param link значение ссылки
 * @param tags теги
 * @param filters фильтры
 */
public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}

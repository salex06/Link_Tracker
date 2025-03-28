package backend.academy.dto;

import java.util.List;

/**
 * Объект передачи данных для ответа на запрос на добавление ссылки
 *
 * @param id идентификатор ссылки
 * @param url значение ссылки
 * @param tags теги
 * @param filters фильтры
 */
public record LinkResponse(Long id, String url, List<String> tags, List<String> filters) {}

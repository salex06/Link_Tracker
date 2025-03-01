package backend.academy.dto;

import java.util.List;

/**
 * DTO - ответ на запрос отслеживания ссылки
 *
 * @param id идентификатор ссылки
 * @param url ссылка на ресурс
 * @param tags теги, относящиеся к ссылке
 * @param filters фильтры, относящиеся к ссылке
 */
public record LinkResponse(Long id, String url, List<String> tags, List<String> filters) {}

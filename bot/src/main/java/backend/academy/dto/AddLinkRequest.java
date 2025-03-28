package backend.academy.dto;

import java.util.List;

/**
 * DTO для запроса на отслеживание ресурса по ссылке
 *
 * @param link ссылка на ресурс
 * @param tags теги ресурса
 * @param filters фильтры для специальных режимов отслеживания ресурса
 */
public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}

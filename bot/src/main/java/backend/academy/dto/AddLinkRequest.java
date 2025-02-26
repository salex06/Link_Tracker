package backend.academy.dto;

import java.util.List;

/**
 * DTO для запроса на отслеживание ресурса по ссылке
 *
 * @param url ссылка на ресурс
 * @param tags теги ресурса
 * @param filters фильтры для специальных режимов отслеживания ресурса
 */
public record AddLinkRequest(String url, List<String> tags, List<String> filters) {}

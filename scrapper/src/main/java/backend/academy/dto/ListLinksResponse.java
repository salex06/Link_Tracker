package backend.academy.dto;

import java.util.List;

/** Объект передачи данных для запроса списка отслеживаемых ссылок */
public record ListLinksResponse(List<backend.academy.model.plain.Link> links, Integer size) {}

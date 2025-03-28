package backend.academy.dto;

import backend.academy.model.Link;
import java.util.List;

/**
 * DTO для передачи набора отслеживаемых ссылок
 *
 * @param links Набор ссылок на ресурсы
 * @param size Количество ссылок в DTO
 */
public record ListLinksResponse(List<Link> links, Integer size) {}

package backend.academy.dto;

/**
 * DTO - запрос на прекращение отслеживания ссылки
 *
 * @param link ссылка на ресурс
 */
public record RemoveLinkRequest(String link) {}

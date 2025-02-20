package backend.academy.dto;

/**
 * DTO - ответ на запрос отслеживания ссылки
 *
 * @param id идентификатор ссылки
 * @param url ссылка на ресурс
 */
public record LinkResponse(Long id, String url) {
    public LinkResponse() {
        this(null, null);
    }
}

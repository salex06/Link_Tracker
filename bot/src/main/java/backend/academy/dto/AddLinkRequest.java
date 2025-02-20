package backend.academy.dto;

/**
 * DTO для запроса на отслеживание ресурса по ссылке
 *
 * @param url ссылка на ресурс
 */
public record AddLinkRequest(String url) {
    public AddLinkRequest() {
        this("");
    }
}

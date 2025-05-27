package backend.academy.clients;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.dto.LinkUpdateInfo;
import backend.academy.model.plain.Link;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.client.RestClient;

public abstract class Client {
    protected final Pattern supportedUrl;
    protected final LinkToApiLinkConverter linkConverter;
    protected final RestClient client;

    public Client(Pattern supportedUrl, LinkToApiLinkConverter linkConverter, RestClient restClient) {
        this.supportedUrl = supportedUrl;
        this.linkConverter = linkConverter;
        this.client = restClient;
    }

    /**
     * Определить, поддерживается ли данная ссылка клиентом
     *
     * @param link значение ссылки
     * @return {@code true}, если ссылка поддерживается, иначе - false
     */
    public boolean supportLink(String link) {
        Matcher matcher = supportedUrl.matcher(link);
        return matcher.matches();
    }

    /**
     * Получить обновления ресурса по данной ссылке
     *
     * @param link ссылка на ресурс для поиска обновлений
     * @return {@code List<LinkUpdateInfo>} - список описания обновлений
     */
    public abstract List<LinkUpdateInfo> getUpdates(Link link);

    /**
     * Получить название ресурса, который обрабатывает клиент
     *
     * @return String - название ресурса по ссылке
     */
    public abstract String getSourceName();
}

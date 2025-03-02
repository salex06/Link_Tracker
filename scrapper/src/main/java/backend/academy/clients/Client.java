package backend.academy.clients;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.client.RestClient;

/**
 * Абстрактный клиент, базовый класс для конкретных клиентов, предоставляющих возможности отслеживания ресурсов на
 * обновления
 */
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
     * @return {@code List<String>} - список описания обновлений в текстовом формате
     */
    public abstract List<String> getUpdates(Link link);
}

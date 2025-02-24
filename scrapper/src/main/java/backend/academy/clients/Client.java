package backend.academy.clients;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import backend.academy.model.Link;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.client.RestClient;

public abstract class Client {
    protected final Pattern supportedUrl;
    protected final LinkToApiLinkConverter linkConverter;

    public Client(Pattern supportedUrl, LinkToApiLinkConverter linkConverter) {
        this.supportedUrl = supportedUrl;
        this.linkConverter = linkConverter;
    }

    public boolean supportLink(Link link) {
        Matcher matcher = supportedUrl.matcher(link.getUrl());
        return matcher.matches();
    }

    public abstract List<String> getUpdates(Link link, RestClient client);
}

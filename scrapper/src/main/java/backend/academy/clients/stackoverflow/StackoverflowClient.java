package backend.academy.clients.stackoverflow;

import backend.academy.clients.Client;
import backend.academy.clients.converter.LinkToApiLinkConverter;
import java.util.regex.Pattern;
import org.springframework.web.client.RestClient;

public abstract class StackoverflowClient extends Client {
    public StackoverflowClient(Pattern supportedUrl, LinkToApiLinkConverter linkConverter, RestClient restClient) {
        super(supportedUrl, linkConverter, restClient);
    }

    @Override
    public String getSourceName() {
        return "stackoverflow";
    }
}

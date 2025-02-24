package backend.academy.clients;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientBeans {
    @Bean
    public ClientManager clientManager(List<Client> clientList) {
        return new ClientManager(clientList);
    }

    @Bean
    public LinkToApiLinkConverter soQuestionLinkConverter() {
        return x -> {
            Pattern pattern = Pattern.compile("^https://stackoverflow\\.com/questions/(\\w+)$");
            Matcher matcher = pattern.matcher(x);
            if (matcher.matches()) {
                return String.format(
                        "https://api.stackexchange.com/2.3/questions/%s?site=stackoverflow", matcher.group(1));
            }
            return null;
        };
    }
}

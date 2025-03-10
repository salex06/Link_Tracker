package backend.academy.clients;

import backend.academy.clients.converter.LinkToApiLinkConverter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

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

    @Bean
    LinkToApiLinkConverter soAnswerLinkConverter() {
        return x -> {
            Pattern pattern = Pattern.compile("^https://stackoverflow\\.com/a/(\\d+)$");

            Matcher matcher = pattern.matcher(x);
            if (matcher.matches()) {
                return String.format(
                        "https://api.stackexchange.com/2.3/answers/%s?site=stackoverflow", matcher.group(1));
            }

            return null;
        };
    }

    @Bean
    LinkToApiLinkConverter gitHubRepositoryConverter() {
        return x -> {
            Pattern pattern = Pattern.compile("^https://github.com/(\\w+)/(\\w+)$");

            Matcher matcher = pattern.matcher(x);
            if (matcher.matches()) {
                return String.format("https://api.github.com/repos/%s/%s", matcher.group(1), matcher.group(2));
            }

            return null;
        };
    }

    @Bean
    LinkToApiLinkConverter gitHubSingleIssueConverter() {
        return x -> {
            Pattern pattern = Pattern.compile("^https://github.com/(\\w+)/(\\w+)/issues/(\\d+)$");

            Matcher matcher = pattern.matcher(x);
            if (matcher.matches()) {
                return String.format(
                        "https://api.github.com/repos/%s/%s/issues/%s",
                        matcher.group(1), matcher.group(2), matcher.group(3));
            }

            return null;
        };
    }

    /**
     * Возвращает реализацию функционального интерфейса для конвертации из ссылки на комментарии issue GitHub в ссылку
     * на GitHub Api
     *
     * @return лямбда-функция, вызываемая методом convert()
     */
    @Bean
    LinkToApiLinkConverter gitHubIssueListClientConverter() {
        return x -> {
            Pattern pattern = Pattern.compile("^https://github.com/(\\w+)/(\\w+)/(issues|pulls)$");
            Matcher matcher = pattern.matcher(x);
            if (matcher.matches()) {
                return String.format(
                        "https://api.github.com/repos/%s/%s/%s", matcher.group(1), matcher.group(2), matcher.group(3));
            }
            return null;
        };
    }

    /**
     * Создает объект класса RestClient с установленным базовым url github api
     *
     * @return объект {@code RestClient} - клиент для взаимодействия с github api
     */
    @Bean
    RestClient gitHubClient() {
        return RestClient.builder().baseUrl("https://api.github.com").build();
    }

    @Bean
    RestClient stackOverflowClient() {
        return RestClient.builder().baseUrl("https://api.stackexchange.com").build();
    }
}

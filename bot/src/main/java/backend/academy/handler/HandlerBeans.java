package backend.academy.handler;

import backend.academy.bot.commands.BotCommandsStorage;
import backend.academy.config.properties.ApplicationStabilityProperties;
import backend.academy.handler.impl.HelpMessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class HandlerBeans {
    private final ApplicationStabilityProperties properties;

    @Bean
    public RestClient restClient(@Value("${scrapper.base-url}") String baseUrl) {
        return RestClient.builder()
                .requestFactory(simpleClientHttpRequestFactory())
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public HelpMessageHandler helpMessageHandler() {
        return new HelpMessageHandler(BotCommandsStorage.getCommandDescription());
    }

    private SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout().getConnectTimeout());
        requestFactory.setReadTimeout(properties.getTimeout().getReadTimeout());
        return requestFactory;
    }
}

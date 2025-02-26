package backend.academy.crawler;

import backend.academy.crawler.impl.TrackMessageCrawler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerBeans {
    @Bean
    public MessageCrawler trackMessageCrawler() {
        return new TrackMessageCrawler();
    }
}

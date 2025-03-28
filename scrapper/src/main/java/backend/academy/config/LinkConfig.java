package backend.academy.config;

import backend.academy.clients.ClientManager;
import backend.academy.repository.LinkRepository;
import backend.academy.repository.impl.MapLinkRepository;
import backend.academy.service.LinkService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LinkConfig {
    @Bean
    public MapLinkRepository mapLinkRepository() {
        return new MapLinkRepository();
    }

    @Bean
    public LinkService linkService(LinkRepository linkRepository, ClientManager clientManager) {
        return new LinkService(linkRepository, clientManager);
    }
}

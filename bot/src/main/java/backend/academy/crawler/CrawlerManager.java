package backend.academy.crawler;

import backend.academy.handler.Handler;
import backend.academy.handler.impl.AddTagMessageHandler;
import backend.academy.handler.impl.TrackMessageHandler;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CrawlerManager {
    private final MessageCrawler trackMessageCrawler;
    private final MessageCrawler addTagMessageCrawler;
    private final TrackMessageHandler trackMessageHandler;
    private final AddTagMessageHandler addTagMessageHandler;

    public CrawlerManager(
            @Qualifier("trackCrawler") MessageCrawler trackMessageCrawler,
            @Qualifier("addTagCrawler") MessageCrawler addTagMessageCrawler,
            TrackMessageHandler trackMessageHandler,
            AddTagMessageHandler addTagMessageHandler) {
        this.trackMessageCrawler = trackMessageCrawler;
        this.addTagMessageCrawler = addTagMessageCrawler;

        this.trackMessageHandler = trackMessageHandler;
        this.addTagMessageHandler = addTagMessageHandler;
    }

    public List<Map.Entry<MessageCrawler, Handler>> getCrawlerHandlerPair() {
        return List.of(
                Map.entry(trackMessageCrawler, trackMessageHandler),
                Map.entry(addTagMessageCrawler, addTagMessageHandler));
    }
}

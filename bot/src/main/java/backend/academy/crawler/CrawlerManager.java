package backend.academy.crawler;

import backend.academy.crawler.impl.tags.removetag.RemoveTagMessageCrawler;
import backend.academy.handler.Handler;
import backend.academy.handler.impl.AddTagMessageHandler;
import backend.academy.handler.impl.RemoveTagMessageHandler;
import backend.academy.handler.impl.TrackMessageHandler;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CrawlerManager {
    private final MessageCrawler trackMessageCrawler;
    private final MessageCrawler addTagMessageCrawler;
    private final RemoveTagMessageCrawler removeTagMessageCrawler;
    private final TrackMessageHandler trackMessageHandler;
    private final AddTagMessageHandler addTagMessageHandler;
    private final RemoveTagMessageHandler removeTagMessageHandler;

    public CrawlerManager(
            @Qualifier("trackCrawler") MessageCrawler trackMessageCrawler,
            @Qualifier("addTagCrawler") MessageCrawler addTagMessageCrawler,
            @Qualifier("eraseTagCrawler") RemoveTagMessageCrawler removeTagMessageCrawler,
            TrackMessageHandler trackMessageHandler,
            AddTagMessageHandler addTagMessageHandler,
            RemoveTagMessageHandler removeTagMessageHandler) {
        this.trackMessageCrawler = trackMessageCrawler;
        this.addTagMessageCrawler = addTagMessageCrawler;
        this.removeTagMessageCrawler = removeTagMessageCrawler;

        this.trackMessageHandler = trackMessageHandler;
        this.addTagMessageHandler = addTagMessageHandler;
        this.removeTagMessageHandler = removeTagMessageHandler;
    }

    public List<Map.Entry<MessageCrawler, Handler>> getCrawlerHandlerPair() {
        return List.of(
                Map.entry(trackMessageCrawler, trackMessageHandler),
                Map.entry(addTagMessageCrawler, addTagMessageHandler),
                Map.entry(removeTagMessageCrawler, removeTagMessageHandler));
    }
}

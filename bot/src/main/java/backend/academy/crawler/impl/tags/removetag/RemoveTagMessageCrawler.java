package backend.academy.crawler.impl.tags.removetag;

import backend.academy.crawler.impl.tags.TagMessageCrawler;
import backend.academy.crawler.impl.tags.TagValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("eraseTagCrawler")
public class RemoveTagMessageCrawler extends TagMessageCrawler {
    @Autowired
    public RemoveTagMessageCrawler(@Qualifier("eraseTagValidator") TagValidator tagValidator) {
        super(tagValidator);
    }
}

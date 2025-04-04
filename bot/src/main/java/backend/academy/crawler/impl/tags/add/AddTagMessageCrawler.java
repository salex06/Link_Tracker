package backend.academy.crawler.impl.tags.add;

import backend.academy.crawler.impl.tags.TagMessageCrawler;
import backend.academy.crawler.impl.tags.TagValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("addTagCrawler")
public class AddTagMessageCrawler extends TagMessageCrawler {
    @Autowired
    public AddTagMessageCrawler(@Qualifier("addTagValidator") TagValidator addTagValidator) {
        super(addTagValidator);
    }
}

package backend.academy.crawler;

import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Update;

public interface MessageCrawler {
    DialogStateDTO crawl(Update update);

    AddLinkRequest terminate(Long id);
}
